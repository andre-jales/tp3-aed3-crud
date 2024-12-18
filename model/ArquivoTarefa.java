package model;

import aed3.*;
import util.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ArquivoTarefa extends aed3.Arquivo<Tarefa> {

    private ArvoreBMais<ParCategoriaId> indiceIndiretoCategoria;
    private ArvoreBMais<ParRotuloTarefa> indiceIndiretoRotuloTarefa;
    private ArvoreBMais<ParTarefaRotulo> indiceIndiretoTarefaRotulo;
    private ArvoreBMais<ParNomeId> indiceIndiretoNomeRotulo;
    private ListaInvertida listaInvertida;

    public ArquivoTarefa() throws Exception {
        super("tarefas", Tarefa.class.getConstructor());
        indiceIndiretoCategoria = new ArvoreBMais<>(
                ParCategoriaId.class.getConstructor(),
                4,
                ".\\dados\\indiceCategoria.db");
        indiceIndiretoRotuloTarefa = new ArvoreBMais<>(
                ParRotuloTarefa.class.getConstructor(),
                4,
                ".\\dados\\indiceRotuloTarefa.db");
        indiceIndiretoTarefaRotulo = new ArvoreBMais<>(
                ParTarefaRotulo.class.getConstructor(),
                4,
                ".\\dados\\indiceTarefaRotulo.db");
        indiceIndiretoNomeRotulo = new ArvoreBMais<>(
                ParNomeId.class.getConstructor(),
                4,
                ".\\dados\\indiceNomeRotulo.db");
        listaInvertida = new ListaInvertida(
                4,
                "dados/dicionario.listainv.db",
                "dados/blocos.listainv.db");
    }

    @Override
    public int create(Tarefa t) throws Exception {

        int id = super.create(t);
        TextProcessor txtPcsr = new TextProcessor();
        String[] termos = txtPcsr.processText(t.getNome());

        for (String termo : termos) {
            listaInvertida.create(termo, new ElementoLista(id, txtPcsr.calcularTF(termo, termos)));
        }

        listaInvertida.incrementaEntidades();

        indiceIndiretoCategoria.create(new ParCategoriaId(id, t.getIdCategoria()));
        return id;
    }

    public int create(Tarefa t, ArrayList<Integer> rotulos) throws Exception {

        int id = create(t);

        for (int rotulo : rotulos) {
            indiceIndiretoTarefaRotulo.create(new ParTarefaRotulo(id, rotulo));
            indiceIndiretoRotuloTarefa.create(new ParRotuloTarefa(rotulo, id));
        }

        return id;
    }

    public void newParTarefaRotulo(int idTarefa, int idRotulo) throws Exception {
        indiceIndiretoTarefaRotulo.create(new ParTarefaRotulo(idTarefa, idRotulo));
        indiceIndiretoRotuloTarefa.create(new ParRotuloTarefa(idRotulo, idTarefa));
    }

    public void deleteParTarefaRotulo(int idTarefa, int idRotulo) throws Exception {
        indiceIndiretoTarefaRotulo.delete(new ParTarefaRotulo(idTarefa, idRotulo));
        indiceIndiretoRotuloTarefa.delete(new ParRotuloTarefa(idRotulo, idTarefa));
    }

    public ArrayList<Tarefa> readByCategoria(int categoriaId) throws Exception {
        ArrayList<ParCategoriaId> pciList = indiceIndiretoCategoria.read(new ParCategoriaId(categoriaId));
        ArrayList<Tarefa> tarefas = new ArrayList<>();

        if (pciList.isEmpty()) {
            return tarefas;
        }

        for (ParCategoriaId pci : pciList) {
            Tarefa tarefa = read(pci.getIdTarefa());
            if (tarefa != null) {
                tarefas.add(tarefa);
            }
        }

        return tarefas;
    }

    public ArrayList<Tarefa> readByRotulo(int rotuloId) throws Exception {
        ArrayList<ParRotuloTarefa> prtList = indiceIndiretoRotuloTarefa.read(new ParRotuloTarefa(rotuloId));
        ArrayList<Tarefa> tarefas = new ArrayList<>();

        if (prtList.isEmpty()) {
            return tarefas;
        }

        for (ParRotuloTarefa prt : prtList) {
            Tarefa tarefa = read(prt.getTarefa());
            if (tarefa != null) {
                tarefas.add(tarefa);
            }
        }

        return tarefas;
    }

    public ArrayList<Rotulo> readRotulosByTarefa(int tarefaId) throws Exception {
        ArrayList<ParTarefaRotulo> paresTarefaRotulo = indiceIndiretoTarefaRotulo.read(new ParTarefaRotulo(tarefaId));
        ArrayList<Rotulo> rotulos = new ArrayList<>();

        for (ParTarefaRotulo parTarefaRotulo : paresTarefaRotulo) {
            Rotulo rotulo = new ArquivoRotulo().read(parTarefaRotulo.getRotulo());
            if (rotulo != null) {
                rotulos.add(rotulo);
            }
        }

        return rotulos;
    }

    public boolean delete(int tarefaId) throws Exception {
        Tarefa tarefa = read(tarefaId);

        if (tarefa == null) {
            return false;
        }

        TextProcessor txtPcsr = new TextProcessor();
        String[] termos = txtPcsr.processText(tarefa.getNome());

        for (String termo : termos) {
            listaInvertida.delete(termo, tarefa.getId());
        }

        listaInvertida.decrementaEntidades();

        ArrayList<ParTarefaRotulo> paresTarefaRotulo = indiceIndiretoTarefaRotulo.read(new ParTarefaRotulo(tarefaId));

        for (ParTarefaRotulo parTarefaRotulo : paresTarefaRotulo) {
            indiceIndiretoTarefaRotulo.delete(parTarefaRotulo);
            indiceIndiretoRotuloTarefa.delete(new ParRotuloTarefa(parTarefaRotulo.getRotulo(), tarefaId));
        }

        boolean removed = super.delete(tarefaId);
        if (removed) {
            indiceIndiretoCategoria.delete(new ParCategoriaId(tarefaId, tarefa.getIdCategoria()));
        }

        return removed;
    }

    /*
     * TODO adicionar no método 'update' a atualização dos rótulos associados à
     * tarefa utilizando os índices indiretos indiceIndiretoRotulo e
     * indiceIndiretoTarefa
     */
    @Override
    public boolean update(Tarefa novaTarefa) throws Exception {
        Tarefa tarefaAntiga = read(novaTarefa.getId());
        if (super.update(novaTarefa)) {
            if (tarefaAntiga != null && novaTarefa.getIdCategoria() != tarefaAntiga.getIdCategoria()) {
                indiceIndiretoCategoria.delete(new ParCategoriaId(tarefaAntiga.getId(), tarefaAntiga.getIdCategoria()));
                indiceIndiretoCategoria.create(new ParCategoriaId(novaTarefa.getId(), novaTarefa.getIdCategoria()));
            }
            return true;
        }
        return false;
    }

    public ArrayList<Tarefa> readAll() throws Exception {

        ArrayList<Tarefa> tarefas = new ArrayList<>();
        for (ParCategoriaId pci : indiceIndiretoCategoria.read(null)) {
            Tarefa tarefa = super.read(pci.getIdTarefa());
            if (tarefa != null) {
                tarefas.add(tarefa);
            }
        }
        return tarefas;
    }

    public ArrayList<Tarefa> readAllByTerms(String busca) throws Exception {

        TextProcessor txtPcsr = new TextProcessor();
        String[] termos = txtPcsr.processText(busca);

        ElementoLista elementoTmp;
        ArrayList<ElementoLista> listaFinal = new ArrayList<>();

        for (String termo : termos) {
            float IDF = txtPcsr.calcularIDF(termo);
            ElementoLista[] listaDados = listaInvertida.read(termo);

            for (ElementoLista elemento : listaDados) {
                elementoTmp = new ElementoLista(elemento.getId(), elemento.getFrequencia() * IDF);
                listaFinal.add(elementoTmp);
            }
        }

        Map<Integer, Float> mapSomas = new HashMap<>();

        // Somar as frequências por id
        for (ElementoLista elemento : listaFinal) {
            mapSomas.put(elemento.getId(), mapSomas.getOrDefault(elemento.getId(), 0.0f) + elemento.getFrequencia());
        }

        // Criar uma lista de novos ElementosLista com id único e frequências somadas
        ArrayList<ElementoLista> resultado = new ArrayList<>();
        for (Map.Entry<Integer, Float> entry : mapSomas.entrySet()) {
            resultado.add(new ElementoLista(entry.getKey(), entry.getValue()));
        }

        resultado.sort((e1, e2) -> Float.compare(e2.getFrequencia(), e1.getFrequencia()));

        ArrayList<Tarefa> tarefas = new ArrayList<>();
        for (ElementoLista elemento : resultado) {
            Tarefa tarefa = read(elemento.getId());
            if (tarefa != null) {
                tarefas.add(tarefa);
            }
        }

        return tarefas;
    }

    public ArrayList<Tarefa> readTarefasByRotulo(String nome) throws Exception {
        ArrayList<Tarefa> tarefas = new ArrayList<>();

        ArrayList<ParNomeId> paresRotuloId = indiceIndiretoNomeRotulo.read(new ParNomeId(nome));
        if (paresRotuloId.isEmpty()) {
            return tarefas;
        }
        ParNomeId parNomeId = paresRotuloId.get(0);

        ArrayList<ParRotuloTarefa> paresRotuloTarefa = indiceIndiretoRotuloTarefa
                .read(new ParRotuloTarefa(parNomeId.getId()));

        for (ParRotuloTarefa parRotuloTarefa : paresRotuloTarefa) {
            Tarefa tarefa = read(parRotuloTarefa.getTarefa());
            if (tarefa != null) {
                tarefas.add(tarefa);
            }
        }

        return tarefas;
    }
}
