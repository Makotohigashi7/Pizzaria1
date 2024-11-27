package org.example;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        String connectionString = "mongodb+srv://makotomatias3:makoto7@cluster0.afukr.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0";

        try (MongoClient mongoClient = MongoClients.create(connectionString)) {
            MongoDatabase database = mongoClient.getDatabase("Pizzaria");

            boolean running = true;

            while (running) {

                String[] entities = {"Clientes", "Atendente", "Pedidos", "Produtos", "Vendas", "Entregador", "Entregas", "Estoque",  "Sair"};
                int entityChoice = JOptionPane.showOptionDialog(null,
                        "Escolha uma entidade para gerenciar:",
                        "Menu Principal",
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.INFORMATION_MESSAGE,
                        null,
                        entities,
                        entities[0]);

                if (entityChoice == entities.length - 1 || entityChoice == -1) {
                    running = false;
                    break;
                }

                String collectionName = entities[entityChoice];
                MongoCollection<Document> selectedCollection = database.getCollection(collectionName);

                switch (collectionName) {

                    case "Atendente" -> gerenciarAtendente(selectedCollection);
                    case "Clientes" -> gerenciarClientes(selectedCollection);
                    case "Entregador" -> gerenciarEntregador(selectedCollection);
                    case "Entregas" -> gerenciarEntregas(selectedCollection);
                    case "Estoque" -> gerenciarEstoque(selectedCollection);
                    case "Pedidos" -> gerenciarPedidos(selectedCollection);
                    case "Produtos" -> gerenciarProdutos(selectedCollection);
                    case "Vendas" -> gerenciarVendas(selectedCollection);


                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erro ao conectar com o banco de dados: " + e.getMessage());
        }
    }

    private static void gerenciarAtendente(MongoCollection<Document> collection) {
        gerenciarEntidade(collection, "Atendente", new String[]{"_id", "Nome", "CPF"});
    }
    private static void gerenciarClientes(MongoCollection<Document> collection) {
        gerenciarEntidade(collection, "Clientes", new String[]{"_id", "Nome", "CPF", "Email", "Endereco", "Celular"});
    }

    private static void gerenciarEntregador(MongoCollection<Document> collection) {
        gerenciarEntidade(collection, "Entregador", new String[]{"_id", "Nome", "CPF", "Veiculo", "Celular"});
    }
    private static void gerenciarEntregas(MongoCollection<Document> collection) {
        gerenciarEntidade(collection, "Entregas", new String[]{"_id", "Tipo", "Endereco"});
    }
    private static void gerenciarEstoque(MongoCollection<Document> collection) {
        gerenciarEntidade(collection, "Estoque", new String[]{"_id", "Ingrediente","Quantidade","Valor"});
    }
    private static void gerenciarPedidos(MongoCollection<Document> collection) {
        gerenciarEntidade(collection, "Pedidos", new String[]{"_id", "Descricão","Quantidade","Valor"});
    }
    private static void gerenciarProdutos(MongoCollection<Document> collection) {
        gerenciarEntidade(collection, "Produtos", new String[]{"_id", "Nome", "Valor"});
    }
    private static void gerenciarVendas(MongoCollection<Document> collection) {
        gerenciarEntidade(collection, "Vendas", new String[]{"_id", "Produto", "Quantidade", "Valor"});
    }

    private static void gerenciarEntidade(MongoCollection<Document> collection, String nomeEntidade, String[] campos) {
        boolean managingEntity = true;

        while (managingEntity) {
            String[] actions = {"Cadastrar", "Alterar", "Apagar", "Voltar"};
            int actionChoice = JOptionPane.showOptionDialog(null,
                    "Escolha uma opção: " + nomeEntidade,
                    "Menu - " + nomeEntidade,
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.INFORMATION_MESSAGE,
                    null,
                    actions,
                    actions[0]);

            switch (actionChoice) {
                case 0 -> cadastrarDocumento(collection, nomeEntidade, campos);
                case 1 -> editarDocumento(collection, nomeEntidade, campos[0]); // Usa o primeiro campo como filtro
                case 2 -> excluirDocumento(collection, nomeEntidade, campos[0]); // Usa o primeiro campo como filtro
                case 3, -1 -> managingEntity = false; // Voltar ao menu principal
                default -> JOptionPane.showMessageDialog(null, "Essa opção não existe!");
            }
        }
    }

    private static void cadastrarDocumento(MongoCollection<Document> collection, String nomeEntidade, String[] campos) {
        try {
            Document documento = new Document();
            for (String campo : campos) {
                String valor = JOptionPane.showInputDialog("Digite o valor para o campo: " + campo);
                if (valor == null || valor.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Entrada incorreta. Operação encerrada.");
                    return;
                }
                documento.append(campo, valor.trim());
            }

            collection.insertOne(documento);
            JOptionPane.showMessageDialog(null, nomeEntidade + " adicionado com sucesso!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erro ao tentar alterar " + nomeEntidade + ": " + e.getMessage());
        }
    }

    private static void editarDocumento(MongoCollection<Document> collection, String nomeEntidade, String campoFiltro) {
        try {
            String valorFiltro = JOptionPane.showInputDialog("Digite um valor" + campoFiltro + " para editar:");
            if (valorFiltro == null || valorFiltro.trim().isEmpty()) {
                JOptionPane.showMessageDialog(null, "Entrada incorreta.  Operação encerrada.");
                return;
            }

            Document filtro = new Document(campoFiltro, valorFiltro.trim());
            Document documentoExistente = collection.find(filtro).first();

            if (documentoExistente == null) {
                JOptionPane.showMessageDialog(null, nomeEntidade + " inexistente!");
                return;
            }

            String campoAtualizar = JOptionPane.showInputDialog("Digite o nome do campo para altera-lo:");
            String novoValor = JOptionPane.showInputDialog("Digite um novo valor" + campoAtualizar + ":");
            if (campoAtualizar == null || novoValor == null || campoAtualizar.trim().isEmpty() || novoValor.trim().isEmpty()) {
                JOptionPane.showMessageDialog(null, "Entrada incorreta.  Operação encerrada.");
                return;
            }

            Document novosDados = new Document("$set", new Document(campoAtualizar.trim(), novoValor.trim()));
            collection.updateOne(filtro, novosDados);

            JOptionPane.showMessageDialog(null, nomeEntidade + " Atualizado!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erro ao tentar alterar " + nomeEntidade + ": " + e.getMessage());
        }
    }

    private static void excluirDocumento(MongoCollection<Document> collection, String nomeEntidade, String campoFiltro) {
        try {
            String valorFiltro = JOptionPane.showInputDialog("Digite o valor do campo " + campoFiltro + " para deletar:");
            if (valorFiltro == null || valorFiltro.trim().isEmpty()) {
                JOptionPane.showMessageDialog(null, "Entrada incorreta. A operação foi canceladaa.");
                return;
            }

            Document filtro = new Document(campoFiltro, valorFiltro.trim());
            Document documentoExistente = collection.find(filtro).first();

            if (documentoExistente == null) {
                JOptionPane.showMessageDialog(null, nomeEntidade + " inexistente!");
                return;
            }

            collection.deleteOne(filtro);
            JOptionPane.showMessageDialog(null, nomeEntidade + " excluído com sucesso!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erro ao tentar excluir " + nomeEntidade + ": " + e.getMessage());
        }
    }
}