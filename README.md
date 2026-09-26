# FFUT

Backend do TUFF (TCC) — Spring Boot 3.3.2 / Java 21 / MongoDB / RabbitMQ / JWT.

## Rodando localmente

0. Criar o arquivo de variáveis de ambiente (não versionado) a partir do exemplo e preencher
   `JWT_SIGNATURE_KEY` e `RABBITMQ_PASSWORD`:

   ```bash
   cp .env.example .env
   openssl rand -base64 48   # use como JWT_SIGNATURE_KEY
   ```

   O `.env` é lido automaticamente pelo Spring e pelo `docker compose`.

1. Subir as dependências de infra (MongoDB e RabbitMQ):

   ```bash
   docker compose up -d
   ```

2. Rodar a aplicação (profile `dev` é o ativo por padrão, porta 8080):

   ```bash
   ./mvnw spring-boot:run
   ```

   Por padrão em dev, o envio de e-mail transacional (verificação de conta, recuperação de senha) via
   Resend fica **desabilitado** (`resend.enabled=false`), já que não há `RESEND_API_KEY` configurada
   localmente. Para testar o fluxo de e-mail de verdade, defina no `.env`:

   ```bash
   RESEND_ENABLED=true
   RESEND_API_KEY=<sua-chave>
   ```

3. Registrar um usuário (via tela `/register` do frontend, ou via curl):

   ```bash
   curl -X POST http://localhost:8080/user/ \
     -H "Content-Type: application/json" \
     -d '{
       "login": "seu-email@exemplo.com",
       "password": "senha123",
       "name": "Nome",
       "surname": "Sobrenome",
       "locale": "PORTUGUESE"
     }'
   ```

4. Todo usuário novo nasce com `verified: false`, e o login exige e-mail verificado. Como o envio de
   e-mail está desabilitado em dev por padrão, marque o usuário como verificado direto no Mongo:

   ```bash
   mongosh "mongodb://localhost:27017/ffut" --eval '
   db.users.updateOne(
     { login: "seu-email@exemplo.com" },
     { $set: { verified: true } }
   )'
   ```

   Confirme com:

   ```bash
   mongosh "mongodb://localhost:27017/ffut" --eval 'db.users.findOne({login: "seu-email@exemplo.com"})'
   ```

5. Logar normalmente pelo frontend (`http://localhost:4200/login`) com o e-mail e senha cadastrados.
