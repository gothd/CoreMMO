# CoreMMO ⚔️ (v1.1.0)

> **Plugin de infraestrutura modular para servidores Minecraft (Paper/Spigot), focado em Arquitetura Limpa, Alta Performance e mecânicas de MMORPG profundo.**

O **CoreMMO** foi completamente refatorado para o modelo **Open Core Modular**, separando as regras de negócio da infraestrutura técnica. Este projeto serve como a base definitiva para um ecossistema escalável.

---

## 🏗️ Arquitetura Modular (Enterprise Grade)

O projeto é dividido em quatro camadas distintas para garantir desacoplamento e facilidade de manutenção:

* **`CoreMMO-API`:** Contém as interfaces, contratos e o `CoreRegistry`. É o que permite que outros desenvolvedores criem extensões (Addons) para o seu servidor sem acessar o código-fonte principal.
* **`CoreMMO-Common`:** Onde vivem os modelos puros (POJOs) e Enums (como `RPGClass`). É Java puro, sem dependências de Bukkit ou Banco de Dados, permitindo o reuso em outros ambientes.
* **`CoreMMO-Infra`:** Camada de persistência. Gerencia o pool de conexões **HikariCP** e a comunicação com **MariaDB** através do padrão DAO.
* **`CoreMMO-Gameplay`:** O "cérebro" do jogo. Implementa as habilidades, menus, guildas, NPCs, loot, balanceamento de atributos e a lógica de conexão dos jogadores.

---

## ✨ Funcionalidades Principais

### ⚡ Performance & Segurança
* **HikariCP Connection Pool:** Gerenciamento profissional de conexões SQL para evitar gargalos.
* **Async IO Engine:** Todas as operações de banco de dados (Login, Save, Guildas) rodam em threads separadas, mantendo o TPS cravado em 20.
* **Sistema Auth e Permissões:** Gerenciador de permissões injetáveis persistido em SQL (Grant/Revoke), desvinculando o servidor do sistema Vanilla de OP.
* **Proteção de Inventário (Level Gating):** Sistema blindado anti-exploit que impede uso de itens superiores ao nível do jogador (Armor Swapping, Hotbar, Offhand).

### 🎮 Sistemas de Jogo (v1.1.0)
* **Progressão e Leveling:** Monstros escalam dano e vida baseados no seu nível de forma logarítmica.
* **Loot Inteligente e Tiers:** Sistema de gerador de itens RNG com 14 Tiers (Comum ao Divino), suporte a NBT e distribuição de dano (Last Hit, Contribution, Instantiated).
* **Armadura Virtual Caped:** Sistema de defesa que ignora as reduções do Minecraft Vanilla, aplicando uma fórmula RPG com cap visual exato de 400 de defesa.
* **Penalidade de Morte:** Perda percentual de XP customizável e proteção "Soulbound" inteligente (itens RPG não caem no chão ao morrer).
* **Classes & Skills dinâmicas:** RayTrace com detecção de Headshot humanóide (Arqueiro), Dano em Área (Guerreiro) e Projéteis mágicos (Mago).
* **NPC Manager Avançado:** Setup de hologramas via comando *in-game*, com Auto-Cleanup de "fantasmas" no carregamento de chunks.
* **Economia & Guildas:** Sistema de transferência monetária segura e criação de clãs.

---

## 🛠️ Comandos e Permissões

| Comando | Descrição | Permissão |
| --- | --- | --- |
| `/classe` | Abre o menu de seleção de classes | Padrão |
| `/stats` | Exibe seus atributos de Vida, Mana e Defesa Real | Padrão |
| `/saldo` | Consulta suas moedas no cache | Padrão |
| `/pagar` | Transfere valores para outro jogador | Padrão |
| `/guilda` | Gerencia criação e info de clãs | Padrão |
| `/grant` / `/revoke` | Gerencia permissões SQL | `coremmo.admin` |
| `/kick` | Expulsa jogadores com motivo customizado | `coremmo.mod` |
| `/npc set <id>` | Seta a posição do NPC na sua localização atual | `coremmo.admin` |
| `/reloadmmo` | Recarrega as configurações e tabelas de Loot | `coremmo.admin` |

---

## 📦 Como Compilar e Rodar

Este projeto utiliza **Maven Multi-Module**. Para gerar o plugin final:

1. Certifique-se de estar usando o **JDK 21** (ver `.sdkmanrc`).
2. Na raiz do projeto, execute:
```bash
mvn clean install
```
