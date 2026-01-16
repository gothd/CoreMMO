# CoreMMO ⚔️ (v1.0.0)

> **Plugin de infraestrutura modular para servidores Minecraft (Paper/Spigot), focado em Arquitetura Limpa e Alta Performance.**

O **CoreMMO** foi completamente refatorado para o modelo **Open Core Modular**, separando as regras de negócio da infraestrutura técnica. Este projeto serve como a base definitiva para um ecossistema de MMORPG escalável.

---

## 🏗️ Arquitetura Modular (Enterprise Grade)

O projeto é dividido em quatro camadas distintas para garantir desacoplamento e facilidade de manutenção:

* **`CoreMMO-API`:** Contém as interfaces, contratos e o `CoreRegistry`. É o que permite que outros desenvolvedores criem extensões (Addons) para o seu servidor sem acessar o código-fonte principal.
* **`CoreMMO-Common`:** Onde vivem os modelos puros (POJOs) e Enums (como `RPGClass`). É Java puro, sem dependências de Bukkit ou Banco de Dados, permitindo o reuso em outros ambientes.
* **`CoreMMO-Infra`:** Camada de persistência. Gerencia o pool de conexões **HikariCP** e a comunicação com **MariaDB** através do padrão DAO.
* **`CoreMMO-Gameplay`:** O "cérebro" do jogo. Implementa as habilidades, menus, sistemas de guilda, NPCs e a lógica de conexão dos jogadores.

---

## ✨ Funcionalidades Principais

### ⚡ Performance & Segurança

* **HikariCP Connection Pool:** Gerenciamento profissional de conexões SQL para evitar gargalos.
* **Async IO Engine:** Todas as operações de banco de dados (Login, Save, Guildas) rodam em threads separadas, mantendo o TPS do servidor em 20.
* **Smart RAM Cache:** Dados dos jogadores e guildas são mantidos em cache para acesso instantâneo durante o combate.
* **Graceful Shutdown:** Sistema de salvamento emergencial e fechamento seguro de conexões em caso de queda do servidor.

### 🎮 Sistemas de Jogo

* **Sistema de Classes & Skills:** Arqueiro (Crossbow/RayTrace), Mago (Fireball) e Guerreiro (Heavy Strike) com sistema de cooldown e mana.
* **NPC Manager Dinâmico:** NPCs configuráveis via `config.yml` com suporte a diálogos múltiplos e holografia automática.
* **Economia & Guildas:** Sistema de transferência de moedas e criação de clãs com persistência assíncrona.
* **UI Avançada:** Scoreboard sem "flicker" usando Teams e Action Bar dinâmica para status de Vida/Mana.

---

## 🛠️ Comandos e Permissões

| Comando | Descrição | Aliases |
| --- | --- | --- |
| `/classe` | Abre o menu de seleção de classes | `/job`, `/profissao` |
| `/saldo` | Consulta suas moedas no cache | `/money`, `/bal` |
| `/pagar` | Transfere valores para outro jogador | `/pay` |
| `/guilda` | Gerencia criação e info de clãs | `/clan`, `/g` |

---

## 📦 Como Compilar e Rodar

Este projeto utiliza **Maven Multi-Module**. Para gerar o plugin final:

1. Certifique-se de estar usando o **JDK 21** (ver `.sdkmanrc`).
2. Na raiz do projeto, execute:
```bash
mvn clean install
```


3. O plugin unificado (Shaded) será gerado em:
   `CoreMMO-Gameplay/target/CoreMMO.jar`.
4. Coloque o arquivo na pasta `plugins/` e configure o `config.yml` com seus dados de MariaDB.
5. Defina um local de spawn definitivo para os NPCs no `config.yml`, ou você pode precisar removê-los através de comandos OPs mais tarde.
---

## 📜 Créditos e Desenvolvimento

Desenvolvido por **[Ruan Oliveira Sena/gothd]** como parte do ecossistema de aprendizado do livro *"Construindo seu Primeiro MMORPG em Java"*.