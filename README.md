# CoreMMO ⚔️

> **O código-fonte oficial do livro "Construindo seu Primeiro MMORPG em Java".**

O **CoreMMO** é um plugin de infraestrutura para servidores Minecraft (Paper/Spigot), desenvolvido com foco em Arquitetura de Software Limpa e Engenharia de Dados.

---

## 📚 Domine a Engenharia de Plugins

Este repositório contém o **código-fonte final** do projeto. Você pode baixá-lo e usar, mas o verdadeiro tesouro é saber **como construí-lo**.

Você quer entender a lógica por trás de cada sistema? Quer deixar de ser um "copiador de código" e se tornar um desenvolvedor capaz de criar seus próprios sistemas de Classes, Habilidades e Banco de Dados?

O E-book **"Construindo seu Primeiro MMORPG em Java"** é o guia definitivo que te leva do "Hello World" até o Deploy Profissional.

🚀 **O que você vai dominar:**

- **Arquitetura Real:** Chega de código bagunçado. Aprenda MVC, DAO e Singleton.
- **Banco de Dados:** Como integrar SQL com Minecraft de forma segura e performática.
- **Engenharia de Jogos:** Polimorfismo, Threads Assíncronas e Manipulação de Pacotes.
- **Infraestrutura:** Como colocar seu servidor online para o mundo sem abrir portas no roteador.

🎓 **Pare de adivinhar e comece a construir.**

👉 **[ADQUIRA O GUIA COMPLETO NA HOTMART](https://go.hotmart.com/V103462605I)**
_(Oferta de Lançamento: Apenas R$ 14,95)_

---

## 🚀 Tecnologias e Arquitetura

Este projeto foi construído para ensinar engenharia de software no ecossistema Minecraft:

- **Linguagem:** Java 21 (Modern Java).
- **API:** Paper API (1.20/1.21).
- **Banco de Dados:** SQLite com padrão **DAO** (Data Access Object) e `PreparedStatement`.
- **Design Patterns:** MVC (Model-View-Controller), Singleton e Strategy (Habilidades).
- **UI/UX:** Adventure API para Action Bars com sistema de **Prioridade de Renderização**.

## ✨ Funcionalidades Implementadas

### 1. Sistema de Classes (Polimorfismo)

- **Guerreiro:** Regeneração de vida passiva e resistência.
- **Arqueiro:** Disparo instantâneo (sem "puxar" corda) e partículas de rastro.
- **Mago:** Disparo de magia (Bola de Fogo) e sistema de Mana.

### 2. Engenharia de Dados

- **Persistência SQL:** Salva classe, mana, vida e localização do jogador.
- **Carregamento Assíncrono:** Previne "lag" no servidor rodando queries em threads separadas.
- **Login Seguro:** Sistema "Anti-Glitch" que aplica invulnerabilidade e cegueira enquanto os dados carregam do banco.

### 3. Interface (GUI)

- **Hotbar Dinâmica:** Mostra Vida e Mana em tempo real na Action Bar.
- **Prioridade Visual:** Avisos de erro/cooldown pausam a barra de status, mas dano recebido força atualização imediata.
- **Menu de Seleção:** Inventário interativo (GUI) para escolher a classe.

## 📦 Como Rodar

1.  Clone este repositório.
2.  Compile o projeto usando Maven (`mvn clean package`).
3.  Pegue o `.jar` gerado na pasta `target/`.
4.  Coloque na pasta `plugins/` do seu servidor PaperMC.

### 🌍 Deploy e Acesso Externo

Para jogar com amigos sem precisar configurar o roteador, o livro inclui um capítulo dedicado ao **Playit.gg**.

Lá ensinamos a:

- Configurar um túnel seguro.
- Criar um **Launcher Automático (.bat)** localmente na sua máquina para iniciar o servidor e a conexão juntos.

👉 _Consulte o capítulo "Publicando o Servidor" no E-book._

## 🛠️ Comandos Disponíveis

| Comando   | Descrição                                 |
| :-------- | :---------------------------------------- |
| `/classe` | Abre o menu GUI para escolher sua Classe. |
| `/curar`  | Recupera vida e mana total.               |
| `/espada` | Dá a "Excalibur".                         |

---

Desenvolvido por **[Ruan Oliveira Sena/Gothd]**.
