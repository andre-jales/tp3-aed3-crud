package view;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Scanner;

import model.Categoria;
import model.Tarefa;
import model.Rotulo;

public class VisaoTarefas {
    private static Scanner sc = new Scanner(System.in);

    public Tarefa leTarefa(int idCategoria) {
        System.out.println("\nInclusão de Tarefa");
        System.out.print("Nome da tarefa: ");
        String nome = sc.nextLine();

        short prioridade;
        do {
            System.out.print("Prioridade (1 a 3): ");
            prioridade = Short.parseShort(sc.nextLine());
            if (prioridade < 1 || prioridade > 3) {
                System.out.println("Prioridade deve ser entre 1 e 3. Tente novamente.");
            }
        } while (prioridade < 1 || prioridade > 3);

        return new Tarefa(idCategoria, nome, LocalDate.now(), (short) 0, prioridade);
    }

    public String leBusca() {
        System.out.print("Termos da busca: ");
        String busca = sc.nextLine();
        return busca;
    }

    public int leIdTarefa() {
        System.out.print("Digite o ID da tarefa: ");
        return Integer.parseInt(sc.nextLine());
    }

    public void mostraTarefa(Tarefa tarefa) {
        System.out.println(tarefa.toString());
    }

    public int menu() {
        System.out.println("PUCTASK 1.0");
        System.out.println("-----------");
        System.out.println("> Início > Tarefas");
        System.out.println("1) Incluir");
        System.out.println("2) Buscar por ID");
        System.out.println("3) Buscar por termos");
        System.out.println("4) Alterar");
        System.out.println("5) Excluir");
        System.out.println("6) Listar");
        System.out.println("7) Listar por Categoria");
        System.out.println("8. Listar Rótulos de Tarefa");
        System.out.println("0) Retornar ao menu anterior");

        System.out.print("Opção: ");
        try {
            return Integer.parseInt(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public void mostrarCategorias(ArrayList<Categoria> categorias) {
        System.out.println("\nCategorias disponíveis:");
        for (int i = 0; i < categorias.size(); i++) {
            System.out.println((i + 1) + ". " + categorias.get(i).getNome());
        }
    }

    public int selecionaCategoria(int totalCategorias) {
        int numeroCategoria;
        do {
            System.out.print("Selecione a categoria da tarefa: ");
            numeroCategoria = Integer.parseInt(sc.nextLine());
            if (numeroCategoria < 1 || numeroCategoria > totalCategorias) {
                System.out.println("Número de categoria inválido. Tente novamente.");
            }
        } while (numeroCategoria < 1 || numeroCategoria > totalCategorias);
        return numeroCategoria;
    }

    public void mostrarRotulos(ArrayList<Rotulo> rotulos) {
        System.out.println("\nRotulos disponíveis:");
        for (int i = 0; i < rotulos.size(); i++) {
            System.out.println((i + 1) + ". " + rotulos.get(i).getRotulo());
        }
    }

    public int selecionaRotulo(int totalRotulos) {
        int numeroRotulo;
        do {
            System.out.print("Selecione a categoria da tarefa: ");
            numeroRotulo = Integer.parseInt(sc.nextLine());
            if (numeroRotulo < 1 || numeroRotulo > totalRotulos) {
                System.out.println("Número de categoria inválido. Tente novamente.");
            }
        } while (numeroRotulo < 1 || numeroRotulo > totalRotulos);
        return numeroRotulo;
    }
}
