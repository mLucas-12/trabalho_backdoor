# Backdoor Acadêmico — Reverse Shell em Java

> **Disciplina:** Segurança da Informação | **Curso:** Engenharia de Software
>
> ⚠️ **Este projeto é exclusivamente acadêmico. Execute APENAS em ambiente local/controlado (loopback 127.0.0.1 ou VMs dedicadas). É estritamente proibido implantar ou testar em sistemas de terceiros sem autorização formal.**

---

## Integrantes

| Nome Completo | Matrícula |
|---|---|
| *(seu nome aqui)* | *(sua matrícula)* |

---

## Visão Geral

Este projeto implementa um **Reverse Shell** — modalidade de backdoor onde a **máquina vítima** inicia a conexão TCP de volta para a **máquina atacante**. Isso inverte o sentido esperado da comunicação, dificultando detecção por firewalls simples que bloqueiam conexões de entrada mas permitem as de saída.

### Arquitetura

```
[Atacante / AttackerServer]  <──── TCP 4444 ────  [Vítima / BackdoorClient]
 - Aguarda conexão                                  - Conecta ao atacante
 - Envia comandos via socket                        - Executa comandos no SO
 - Exibe a saída recebida                           - Retorna a saída
```

### Protocolo de Comunicação

| Direção | Conteúdo |
|---|---|
| Atacante → Vítima | String com o comando a executar |
| Vítima → Atacante | Linhas de saída do comando |
| Vítima → Atacante | `##END##` (marcador de fim de resposta) |

---

## Estrutura do Repositório

```
/
├── src/
│   ├── AttackerServer.java   # Servidor — lado do atacante (listener)
│   └── BackdoorClient.java   # Cliente — lado da vítima (reverse shell)
├── .gitignore
├── README.md
└── LICENSE
```

---

## Compilação e Execução

### Pré-requisitos
- Java JDK 11 ou superior instalado
- `javac` e `java` disponíveis no PATH

### 1. Compilar

```bash
# A partir da raiz do projeto
javac -d out src/AttackerServer.java src/BackdoorClient.java
```

Os arquivos `.class` serão gerados na pasta `out/`.

### 2. Executar (mesma máquina — loopback 127.0.0.1)

**Terminal 1 — Iniciar o servidor (atacante):**
```bash
java -cp out AttackerServer
```

**Terminal 2 — Iniciar o backdoor (vítima):**
```bash
java -cp out BackdoorClient
```

> O `BackdoorClient` conecta por padrão a `127.0.0.1:4444`.  
> Para testar em VMs distintas, altere a constante `ATTACKER_IP` em `BackdoorClient.java`.

---

## Demonstração de Uso

### Windows (cmd.exe)

```
[*] Servidor iniciado. Aguardando conexão na porta 4444...
[+] Conexão recebida de: 127.0.0.1
[*] Sessão ativa. Digite comandos ('exit' para encerrar):

shell> whoami
desktop-abc\usuario

shell> dir C:\Users
 Volume in drive C is Windows
 ...

shell> ipconfig
Windows IP Configuration
   IPv4 Address. . . : 127.0.0.1
   ...

shell> exit
[*] Encerrando conexão...
[*] Servidor encerrado.
```

### Linux (/bin/sh)

```
shell> whoami
root

shell> ls /etc
passwd  shadow  hosts  ...

shell> uname -a
Linux kali 5.15.0-kali2-amd64 ...

shell> exit
```

---

## Análise Teórica de Segurança

### Conceito Explorado

O backdoor implementado utiliza o conceito de **Reverse Shell**: ao invés de abrir uma porta em escuta na máquina vítima (Bind Shell), é a própria vítima que estabelece a conexão de saída com o atacante. Isso explora a assimetria típica de firewalls corporativos, que bloqueiam conexões TCP de *entrada* mas permitem as de *saída* (por exemplo, para acesso à internet). Uma vez estabelecida a conexão, o atacante obtém execução arbitrária de comandos no shell do sistema operacional da vítima (`cmd.exe` no Windows ou `/bin/sh` no Linux), podendo exfiltrar dados, mover-se lateralmente na rede ou instalar persistência.

### Contramedidas — Blue Team

Uma equipe de defesa pode detectar e mitigar esse backdoor de diversas formas:

| Técnica de Defesa | Como Detecta/Mitiga |
|---|---|
| **Firewall de saída (egress filtering)** | Bloqueia conexões TCP de saída para portas não autorizadas (ex.: porta 4444). |
| **IDS/IPS (ex.: Snort, Suricata)** | Identifica padrões de tráfego de reverse shells por assinaturas ou comportamento anômalo. |
| **Monitoramento de processos** | Detecta `java.exe` ou `sh` filhos de processos não esperados; ferramentas como Sysmon (Windows) ou auditd (Linux) registram a criação de processos. |
| **EDR (Endpoint Detection & Response)** | Correlaciona eventos de rede + execução de processos, alertando para `ProcessBuilder`/`Runtime.exec()` com conexões ativas. |
| **Análise de portas abertas** | `netstat -antp` ou `ss -tnp` revelam conexões estabelecidas suspeitas e o PID do processo responsável. |
| **Allowlist de aplicações** | Impede a execução de JARs ou classes Java não autorizados na máquina. |
| **SIEM + Log Correlation** | Correlaciona logs de rede e de sistema para identificar padrões de C2 (Command & Control). |

---

## Licença

Distribuído sob a licença MIT. Consulte o arquivo [LICENSE](LICENSE).
