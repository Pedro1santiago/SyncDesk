# SyncDesk — Documentação da API

## O que é o SyncDesk?

O **SyncDesk** é um sistema web de **gerenciamento de chamados corporativos** (help desk interno). Ele centraliza todas as solicitações de suporte feitas pelos colaboradores de uma empresa, permitindo que problemas, dúvidas e demandas internas sejam registrados, acompanhados e resolvidos de forma organizada.

O sistema funciona como um canal único de comunicação entre funcionários e equipes de suporte, mantendo histórico completo de cada atendimento.

---

## Para que serve?

- Um funcionário **abre um ticket** descrevendo seu problema ou solicitação
- O ticket é **direcionado para um departamento** (ex: TI, RH, Financeiro)
- Um **atendente (agent) assume** o chamado e inicia o atendimento
- A comunicação acontece via **mensagens** vinculadas ao ticket
- **Arquivos e prints** podem ser anexados ao chamado
- O status evolui até o ticket ser **resolvido e fechado**

---

## Perfis de Acesso (Roles)

| Role | Descrição |
|------|-----------|
| `USER` | Funcionário comum — abre tickets, envia mensagens, anexa arquivos, acompanha seus chamados |
| `AGENT` | Atendente — assume tickets, altera status, responde mensagens |
| `ADMIN` | Administrador — gerencia usuários, departamentos e visualiza todos os tickets |

---

## Autenticação

O sistema usa **JWT (JSON Web Token)**. Todo endpoint (exceto login e registro) exige o token no header:

```
Authorization: Bearer <seu_token_aqui>
```

O token é retornado no login e tem validade de **24 horas** por padrão.

---

## Status dos Tickets

| Status | Descrição |
|--------|-----------|
| `OPEN` | Ticket recém-criado, aguardando atribuição |
| `IN_PROGRESS` | Atendente assumiu o chamado |
| `WAITING_CUSTOMER` | Atendente aguarda resposta do usuário |
| `RESOLVED` | Chamado resolvido, aguardando confirmação |
| `CLOSED` | Encerrado — nenhuma alteração é permitida |

> ⚠️ **Tickets com status `CLOSED` são imutáveis**: não aceitam novas mensagens, anexos, mudança de status, prioridade ou departamento.

## Prioridades dos Tickets

| Prioridade | Descrição |
|------------|-----------|
| `LOW` | Baixa urgência |
| `MEDIUM` | Urgência média |
| `HIGH` | Alta urgência |
| `URGENT` | Crítico, requer atenção imediata |

---

## Base URL

```
http://localhost:8080
```

---

## Endpoints

---

### 🔐 Autenticação — `/api/auth`

#### `POST /api/auth/register`
Registra um novo usuário. Todo usuário criado por este endpoint recebe a role `USER`.

**Autenticação:** Não requerida

**Body:**
```json
{
  "username": "joao.silva",
  "email": "joao@empresa.com",
  "password": "senha123"
}
```

| Campo | Tipo | Validação |
|-------|------|-----------|
| `username` | string | obrigatório, 3–50 caracteres |
| `email` | string | obrigatório, formato de email válido |
| `password` | string | obrigatório, mínimo 6 caracteres |

**Resposta `201 Created`:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "email": "joao@empresa.com",
  "role": "USER"
}
```

---

#### `POST /api/auth/login`
Autentica um usuário e retorna o JWT.

**Autenticação:** Não requerida

**Body:**
```json
{
  "email": "joao@empresa.com",
  "password": "senha123"
}
```

**Resposta `200 OK`:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "email": "joao@empresa.com",
  "role": "USER"
}
```

**Erros possíveis:**
- `401 Unauthorized` — credenciais inválidas

---

### 🏢 Departamentos — `/api/departments`

#### `GET /api/departments`
Lista todos os departamentos.

**Autenticação:** Requerida (qualquer role)

**Resposta `200 OK`:**
```json
[
  {
    "id": "uuid-aqui",
    "name": "TI"
  },
  {
    "id": "uuid-aqui",
    "name": "Financeiro"
  }
]
```

---

#### `GET /api/departments/{id}`
Busca um departamento pelo ID.

**Autenticação:** Requerida (qualquer role)

**Resposta `200 OK`:**
```json
{
  "id": "uuid-aqui",
  "name": "TI"
}
```

**Erros:** `404 Not Found`

---

#### `POST /api/departments`
Cria um novo departamento.

**Autenticação:** `ADMIN`

**Body:**
```json
{
  "name": "Jurídico"
}
```

**Resposta `201 Created`:**
```json
{
  "id": "uuid-gerado",
  "name": "Jurídico"
}
```

**Erros:** `422 Unprocessable Entity` se o nome já existe

---

#### `PUT /api/departments/{id}`
Renomeia um departamento.

**Autenticação:** `ADMIN`

**Body:**
```json
{
  "name": "Jurídico e Compliance"
}
```

**Resposta `200 OK`:** departamento atualizado

---

#### `DELETE /api/departments/{id}`
Remove um departamento.

**Autenticação:** `ADMIN`

**Resposta `204 No Content`**

---

### 👤 Usuários — `/api/users`

#### `GET /api/users`
Lista todos os usuários com paginação.

**Autenticação:** `ADMIN`

**Query params:**
| Param | Padrão | Descrição |
|-------|--------|-----------|
| `page` | `0` | Número da página |
| `size` | `20` | Tamanho da página |
| `sort` | — | Ex: `username,asc` |

**Resposta `200 OK`:**
```json
{
  "content": [
    {
      "id": "uuid",
      "username": "joao.silva",
      "email": "joao@empresa.com",
      "role": "USER",
      "department": "TI",
      "createdAt": "2026-01-15T10:30:00"
    }
  ],
  "totalElements": 42,
  "totalPages": 3,
  "size": 20,
  "number": 0
}
```

---

#### `GET /api/users/me`
Retorna os dados do usuário autenticado.

**Autenticação:** Requerida (qualquer role)

**Resposta `200 OK`:**
```json
{
  "id": "uuid",
  "username": "joao.silva",
  "email": "joao@empresa.com",
  "role": "USER",
  "department": "TI",
  "createdAt": "2026-01-15T10:30:00"
}
```

---

#### `GET /api/users/{id}`
Busca um usuário por ID.

**Autenticação:** `ADMIN`

**Resposta `200 OK`:** mesmo formato do `/me`

---

#### `POST /api/users`
Cria um usuário (admin pode definir role e departamento).

**Autenticação:** `ADMIN`

**Body:**
```json
{
  "username": "carlos.ti",
  "email": "carlos@empresa.com",
  "password": "senha123",
  "role": "AGENT",
  "departmentId": "uuid-do-departamento"
}
```

| Campo | Tipo | Obrigatório | Descrição |
|-------|------|-------------|-----------|
| `username` | string | sim | 3–50 chars |
| `email` | string | sim | email válido |
| `password` | string | sim | mín. 6 chars |
| `role` | string | sim | `USER`, `AGENT` ou `ADMIN` |
| `departmentId` | UUID | não | ID do departamento |

**Resposta `201 Created`:** usuário criado

---

#### `PUT /api/users/{id}`
Atualiza role e/ou departamento de um usuário.

**Autenticação:** `ADMIN`

**Body:**
```json
{
  "role": "AGENT",
  "departmentId": "uuid-do-departamento"
}
```

> Ambos os campos são opcionais — envie apenas o que deseja alterar.

**Resposta `200 OK`:** usuário atualizado

---

### 🎫 Tickets — `/api/tickets`

#### `POST /api/tickets`
Cria um novo ticket.

**Autenticação:** Requerida (qualquer role)

**Body:**
```json
{
  "title": "Não consigo acessar meu e-mail",
  "description": "Após trocar minha senha, o Outlook informa que a senha está incorreta.",
  "priority": "HIGH",
  "departmentId": "uuid-do-departamento-ti"
}
```

| Campo | Tipo | Obrigatório | Validação |
|-------|------|-------------|-----------|
| `title` | string | sim | 5–100 caracteres |
| `description` | string | sim | mínimo 10 caracteres |
| `priority` | string | sim | `LOW`, `MEDIUM`, `HIGH`, `URGENT` |
| `departmentId` | UUID | não | direciona para um departamento |

**Resposta `201 Created`:**
```json
{
  "id": "uuid-do-ticket",
  "title": "Não consigo acessar meu e-mail",
  "description": "Após trocar minha senha, o Outlook informa que a senha está incorreta.",
  "status": "OPEN",
  "priority": "HIGH",
  "user": {
    "id": "uuid",
    "username": "joao.silva",
    "email": "joao@empresa.com",
    "role": "USER",
    "department": "TI",
    "createdAt": "2026-01-15T10:30:00"
  },
  "assignedUser": null,
  "departments": ["TI"],
  "createdAt": "2026-05-31T14:00:00",
  "updatedAt": "2026-05-31T14:00:00",
  "closedAt": null
}
```

---

#### `GET /api/tickets`
Lista tickets com paginação. O resultado depende da role:
- `ADMIN` → vê **todos** os tickets
- `AGENT` → vê apenas os tickets **atribuídos a ele**
- `USER` → vê apenas os **seus próprios** tickets

**Autenticação:** Requerida (qualquer role)

**Query params:**
| Param | Padrão | Descrição |
|-------|--------|-----------|
| `page` | `0` | Número da página |
| `size` | `20` | Itens por página |
| `sort` | — | Ex: `createdAt,desc` |

**Resposta `200 OK`:** página de `TicketResponse` (mesmo formato do POST)

---

#### `GET /api/tickets/{id}`
Busca um ticket pelo ID.

**Autenticação:** Requerida — apenas quem criou, quem está atribuído, ou `ADMIN`

**Resposta `200 OK`:** `TicketResponse` completo

**Erros:** `404 Not Found`, `422 Unprocessable Entity` (sem acesso)

---

#### `PATCH /api/tickets/{id}/assign`
Atribui um atendente ao ticket. Se o ticket estava `OPEN`, muda automaticamente para `IN_PROGRESS`.

**Autenticação:** `ADMIN` ou `AGENT`

**Body:**
```json
{
  "agentId": "uuid-do-atendente"
}
```

**Resposta `200 OK`:** ticket atualizado com `assignedUser` preenchido

---

#### `PATCH /api/tickets/{id}/status`
Altera o status do ticket.

**Autenticação:** `ADMIN` ou `AGENT`

**Body:**
```json
{
  "status": "WAITING_CUSTOMER"
}
```

Valores aceitos: `OPEN`, `IN_PROGRESS`, `WAITING_CUSTOMER`, `RESOLVED`, `CLOSED`

**Resposta `200 OK`:** ticket atualizado

---

#### `PATCH /api/tickets/{id}/priority`
Altera a prioridade do ticket.

**Autenticação:** `ADMIN` ou `AGENT`

**Body:**
```json
{
  "priority": "URGENT"
}
```

Valores aceitos: `LOW`, `MEDIUM`, `HIGH`, `URGENT`

**Resposta `200 OK`:** ticket atualizado

---

#### `PATCH /api/tickets/{id}/department`
Associa um departamento ao ticket.

**Autenticação:** `ADMIN` ou `AGENT`

**Body:**
```json
{
  "departmentId": "uuid-do-departamento"
}
```

**Resposta `200 OK`:** ticket atualizado com o departamento adicionado

---

#### `PATCH /api/tickets/{id}/close`
Fecha o ticket definitivamente. Após fechado, **nenhuma alteração é possível**.

**Autenticação:** `ADMIN` ou `AGENT`

**Body:** nenhum

**Resposta `200 OK`:** ticket com `status: "CLOSED"` e `closedAt` preenchido

---

### 💬 Mensagens — `/api/tickets/{ticketId}/messages`

#### `GET /api/tickets/{ticketId}/messages`
Lista todas as mensagens de um ticket.

**Autenticação:** Requerida — criador, atribuído ou `ADMIN`

**Resposta `200 OK`:**
```json
[
  {
    "id": "uuid",
    "message": "Ainda estou com o problema.",
    "user": {
      "id": "uuid",
      "username": "joao.silva",
      "email": "joao@empresa.com",
      "role": "USER",
      "department": "TI",
      "createdAt": "2026-01-15T10:30:00"
    },
    "createdAt": "2026-05-31T14:05:00"
  }
]
```

---

#### `POST /api/tickets/{ticketId}/messages`
Envia uma nova mensagem no ticket.

**Autenticação:** Requerida — criador, atribuído ou `ADMIN`

> ⚠️ Não é possível enviar mensagem em ticket com status `CLOSED`.

**Body:**
```json
{
  "message": "Pode reiniciar o computador e tentar novamente?"
}
```

| Campo | Tipo | Validação |
|-------|------|-----------|
| `message` | string | obrigatório, 1–2000 caracteres |

**Resposta `201 Created`:**
```json
{
  "id": "uuid",
  "message": "Pode reiniciar o computador e tentar novamente?",
  "user": { ... },
  "createdAt": "2026-05-31T14:10:00"
}
```

---

### 📎 Anexos — `/api/tickets/{ticketId}/attachments`

#### `GET /api/tickets/{ticketId}/attachments`
Lista todos os anexos de um ticket.

**Autenticação:** Requerida — criador, atribuído ou `ADMIN`

**Resposta `200 OK`:**
```json
[
  {
    "id": "uuid",
    "fileName": "print-erro.png",
    "fileUrl": "https://storage.empresa.com/files/print-erro.png",
    "uploadedAt": "2026-05-31T14:15:00"
  }
]
```

---

#### `POST /api/tickets/{ticketId}/attachments`
Adiciona um anexo ao ticket.

**Autenticação:** Requerida — criador, atribuído ou `ADMIN`

> ⚠️ Não é possível anexar arquivos em ticket com status `CLOSED`.

> **Observação:** O backend armazena apenas o nome e a URL do arquivo. O upload do arquivo em si deve ser feito previamente para um serviço de storage (ex: S3, Supabase Storage, Cloudinary) e a URL resultante é enviada neste endpoint.

**Body:**
```json
{
  "fileName": "print-erro.png",
  "fileUrl": "https://storage.empresa.com/files/print-erro.png"
}
```

| Campo | Tipo | Validação |
|-------|------|-----------|
| `fileName` | string | obrigatório, máx. 255 caracteres |
| `fileUrl` | string | obrigatório, máx. 500 caracteres |

**Resposta `201 Created`:**
```json
{
  "id": "uuid",
  "fileName": "print-erro.png",
  "fileUrl": "https://storage.empresa.com/files/print-erro.png",
  "uploadedAt": "2026-05-31T14:15:00"
}
```

---

## Tratamento de Erros

Todos os erros seguem o mesmo formato:

```json
{
  "status": 404,
  "message": "User not found with id: uuid-aqui"
}
```

Para erros de validação (`400`):

```json
{
  "status": 400,
  "message": "Validation failed",
  "errors": {
    "email": "must be a well-formed email address",
    "password": "size must be between 6 and 100"
  }
}
```

### Códigos de erro comuns

| Código | Significado |
|--------|-------------|
| `400 Bad Request` | Dados inválidos no body (validação) |
| `401 Unauthorized` | Token ausente, inválido ou expirado |
| `403 Forbidden` | Token válido, mas sem permissão para a ação |
| `404 Not Found` | Recurso não encontrado |
| `409 Conflict` | Estado inválido (ex: ticket já fechado) |
| `422 Unprocessable Entity` | Regra de negócio violada (ex: email já cadastrado) |

---

## Fluxo Típico de Uso

### 1. Autenticação
```
POST /api/auth/login  →  recebe token JWT
```

### 2. Funcionário abre um ticket
```
POST /api/tickets
  body: { title, description, priority, departmentId }
```

### 3. Atendente vê seus tickets
```
GET /api/tickets  →  retorna apenas os atribuídos ao agente logado
```

### 4. Admin atribui o ticket ao atendente
```
PATCH /api/tickets/{id}/assign
  body: { agentId }
```

### 5. Conversa acontece
```
POST /api/tickets/{id}/messages  →  funcionário envia mensagem
POST /api/tickets/{id}/messages  →  atendente responde
GET  /api/tickets/{id}/messages  →  lista o histórico
```

### 6. Arquivo é anexado
```
POST /api/tickets/{id}/attachments
  body: { fileName, fileUrl }
```

### 7. Atendente resolve e fecha
```
PATCH /api/tickets/{id}/status   body: { "status": "RESOLVED" }
PATCH /api/tickets/{id}/close
```

---

## Departamentos padrão (inseridos na inicialização)

- TI
- Financeiro
- RH
- Comercial
- Operações

---

## Configuração e execução local

### Pré-requisitos
- Java 17+
- PostgreSQL rodando localmente

### Banco de dados
Crie o banco antes de iniciar:
```sql
CREATE DATABASE syncdesk;
```

### Variáveis de ambiente / `application.properties`
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/syncdesk
spring.datasource.username=postgres
spring.datasource.password=postgres

api.security.token.secret=<chave-secreta-longa>
api.security.token.expiration=86400000
```

### Subindo o projeto
```bash
./mvnw spring-boot:run
```

O Flyway executa as migrations automaticamente na primeira inicialização, criando todas as tabelas e inserindo os departamentos padrão.

---

## Tecnologias

| Tecnologia | Versão | Uso |
|-----------|--------|-----|
| Java | 17 | Linguagem |
| Spring Boot | 4.0.6 | Framework principal |
| Spring Security | — | Autenticação e autorização |
| JWT (JJWT) | 0.12.3 | Tokens de autenticação |
| Spring Data JPA | — | ORM / acesso a dados |
| PostgreSQL | — | Banco de dados |
| Flyway | — | Migrations de banco |
| Lombok | — | Redução de boilerplate |
| BCrypt | — | Hash de senhas |
