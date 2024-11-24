package model;
import aed3.*;
import util.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ArquivoTarefa extends aed3.Arquivo<Tarefa> {

    private ArvoreBMais<ParCategoriaId> indiceIndiretoCategoria;
    private ListaInvertida listaInvertida;

    public ArquivoTarefa() throws Exception {
        super("tarefas", Tarefa.class.getConstructor());
        indiceIndiretoCategoria = new ArvoreBMais<>(
            ParCategoriaId.class.getConstructor(),
            4,
            ".\\dados\\indiceCategoria.db"
        );
        listaInvertida = new ListaInvertida(
            4, 
            "dados/dicionario.listainv.db", 
            "dados/blocos.listainv.db"
        );
    }

    @Override
    public int create(Tarefa t) throws Exception {

        int id = super.create(t);
        TextProcessor txtPcsr = new TextProcessor();
        String[] termos = txtPcsr.processText(t.getNome());

        for(String termo : termos){
            listaInvertida.create(termo, new ElementoLista(id, txtPcsr.calcularTF(termo, termos)));
        }

        listaInvertida.incrementaEntidades();
        
        indiceIndiretoCategoria.create(new ParCategoriaId(id, t.getIdCategoria()));
        return id;
    }

    public ArrayList<Tarefa> readByCategoria(int categoriaId) throws Exception {
        ArrayList<ParCategoriaId> pciList = indiceIndiretoCategoria.read(new ParCategoriaId(-1, categoriaId));
        ArrayList<Tarefa> tarefas = new ArrayList<>();

        if (pciList.isEmpty()) {
            return tarefas;
        }

        for (ParCategoriaId pci : pciList) {
            Tarefa tarefa = read(pci.getId());
            if (tarefa != null) {
                tarefas.add(tarefa);
            }
        }

        return tarefas;
    }

    public boolean delete(int tarefaId) throws Exception {
        Tarefa tarefa = read(tarefaId);

        if (tarefa == null) {
            return false; 
        }

        TextProcessor txtPcsr = new TextProcessor();
        String[] termos = txtPcsr.processText(tarefa.getNome());

        for(String termo : termos){
            listaInvertida.delete(termo, tarefa.getId());
        }

        listaInvertida.decrementaEntidades();
    
        boolean removed = super.delete(tarefaId);
        if (removed) {
            indiceIndiretoCategoria.delete(new ParCategoriaId(tarefa.getIdCategoria(), tarefaId));
        }
    
        return removed;
    }

    @Override
    public boolean update(Tarefa novaTarefa) throws Exception {
        Tarefa tarefaAntiga = read(novaTarefa.getId());
        if (super.update(novaTarefa)) {
            if (tarefaAntiga != null && novaTarefa.getIdCategoria() != tarefaAntiga.getIdCategoria()) {
                indiceIndiretoCategoria.delete(new ParCategoriaId(tarefaAntiga.getIdCategoria(), tarefaAntiga.getId()));
                indiceIndiretoCategoria.create(new ParCategoriaId(novaTarefa.getIdCategoria(), novaTarefa.getId()));
            }
            return true;
        }
        return false;
    }

    public ArrayList<Tarefa> readAll() throws Exception {

        ArrayList<Tarefa> tarefas = new ArrayList<>();
        for (ParCategoriaId pci : indiceIndiretoCategoria.read(null)) {
            Tarefa tarefa = super.read(pci.getId());
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

        for(String termo : termos){
            float IDF = txtPcsr.calcularIDF(termo);
            ElementoLista[] listaDados = listaInvertida.read(termo);

            for(ElementoLista elemento : listaDados){
                elementoTmp =  new ElementoLista(elemento.getId(), elemento.getFrequencia()*IDF);
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

        // for (ElementoLista elemento : resultado) {
        //     System.out.println("("+ elemento.getId() +", "+ elemento.getFrequencia() +")");
        // }

        ArrayList<Tarefa> tarefas = new ArrayList<>();
        for (ElementoLista elemento : resultado) {
            Tarefa tarefa = read(elemento.getId()); // Obter a tarefa pelo ID
            if (tarefa != null) {
                tarefas.add(tarefa);
            }
        }
    
        return tarefas;
    }
}