# Guía: una tienda por proyecto a partir de GenericAPI

Esta API es **genérica**: cada tienda (cliente) tiene su propio repositorio, creado a partir de este. Los cambios comunes se hacen **siempre aquí primero** y cada tienda decide si se los trae.

```
            GenericAPI (este repo)          ← cambios comunes, versiones v1.0.0, v1.1.0…
              │            │          │
         merge │      merge │          ✗  (congelada)
              ▼            ▼          ▼
         TiendaY-API  TiendaZ-API  TiendaVieja-API
          (activa)     (activa)     (en producción, sin cambios)
```

En cada repo de tienda:

- `origin` → el repo de la tienda (ahí se hace `push`).
- `upstream` → este repo genérico (de ahí se hace `fetch`, **nunca** `push`).

> En los comandos, sustituye `TiendaX` por el nombre real de la tienda y `<hash>` por el hash del commit.

---

## Índice

0. [Preparar el genérico (una sola vez)](#paso-0--preparar-el-genérico-una-sola-vez)
1. [Crear una tienda nueva](#paso-1--crear-una-tienda-nueva)
2. [Llevar un cambio del genérico a las tiendas activas](#paso-2--llevar-un-cambio-del-genérico-a-las-tiendas-activas)
3. [Congelar una tienda terminada](#paso-3--congelar-una-tienda-terminada)
4. [Parche urgente a una tienda congelada](#paso-4--parche-urgente-a-una-tienda-congelada)
5. [Llevar al genérico un arreglo hecho en una tienda](#paso-5--llevar-al-genérico-un-arreglo-hecho-en-una-tienda)
6. [Reglas para evitar conflictos](#reglas-para-evitar-conflictos)
7. [Chuleta](#chuleta)
8. [Registro de tiendas](#registro-de-tiendas)

---

## Paso 0 · Preparar el genérico (una sola vez)

- [ ] **Apuntar el remote al nuevo nombre del repo:**
  ```bash
  cd GenericAPI-REST
  git remote set-url origin git@github.com:ElMostWantedK/GenericAPI.git
  git fetch origin
  ```

- [ ] **Borrar las copias de seguridad de la limpieza de secrets.** Contienen los secrets antiguos; bórralas cuando hayas comprobado que todo está bien:
  ```bash
  git update-ref -d refs/original/refs/heads/master
  git branch -D backup/pre-secret-purge backup/origin-master-before-purge
  ```

- [ ] **Rotar los secrets antiguos**, porque estuvieron públicos en el historial:
  - Google Cloud Console → *APIs & Services → Credentials* → tu OAuth client → **Reset secret**.
  - JWT: genera uno nuevo con `openssl rand -base64 32`.
  - Pon los nuevos valores en tu `.env` local.

- [ ] **Crear `CHANGELOG.md`** con la primera versión:
  ```markdown
  # Changelog

  ## v1.0.0 – Versión inicial
  - PostgreSQL, Docker, secrets en `.env`.
  ```

- [ ] **Publicar la primera versión:**
  ```bash
  git add CHANGELOG.md
  git commit -m "Add CHANGELOG"
  git tag v1.0.0
  git push origin master --tags
  ```

---

## Paso 1 · Crear una tienda nueva

- [ ] **En GitHub:** crea un repo **vacío** (sin README, sin .gitignore, sin licencia), por ejemplo `TiendaX-API`, privado.

- [ ] **Clonar el genérico con su historial.** El historial es necesario para que los merges funcionen después; no copies los ficheros a mano:
  ```bash
  git clone git@github.com:ElMostWantedK/GenericAPI.git TiendaX-API
  cd TiendaX-API
  ```

- [ ] **Configurar los remotes:**
  ```bash
  # El genérico pasa a ser "upstream"
  git remote rename origin upstream

  # El repo de la tienda pasa a ser "origin"
  git remote add origin git@github.com:ElMostWantedK/TiendaX-API.git

  # Seguridad: impide hacer push al genérico desde la tienda por error
  git remote set-url --push upstream NO_PUSH

  git push -u origin master --tags
  ```
  Comprueba con `git remote -v`: `upstream` tiene que mostrar `NO_PUSH` en la línea `(push)`.

- [ ] **Secrets propios de esta tienda.** Nunca copies el `.env` de otra tienda ni el del genérico:
  ```bash
  cp .env.example .env
  ```
  Rellena `.env` con valores **nuevos**:
  - `JWT_SECRET` → `openssl rand -base64 32`
  - `DB_USERNAME` / `DB_PASSWORD` → credenciales propias
  - `GOOGLE_CLIENT_ID` / `GOOGLE_CLIENT_SECRET` → crea un **OAuth client nuevo** en Google Cloud para esta tienda, con sus dominios

- [ ] **Comprobar que arranca:**
  ```bash
  docker compose up -d --build
  curl http://localhost:8080/product/list   # debe devolver 200
  ```

- [ ] **Anotar en el README de la tienda** de qué versión parte:
  ```markdown
  > Basada en GenericAPI v1.0.0
  ```

- [ ] **Añadir la tienda al [Registro de tiendas](#registro-de-tiendas)** de este fichero, en el genérico.

---

## Paso 2 · Llevar un cambio del genérico a las tiendas activas

### 2.1 En el genérico

- [ ] Hacer el cambio en una rama:
  ```bash
  cd GenericAPI-REST
  git checkout master && git pull
  git checkout -b feature/descripcion-corta
  # ... cambios ...
  mvn test
  git commit -am "Descripción del cambio"
  ```

- [ ] Integrarlo en `master`:
  ```bash
  git checkout master
  git merge feature/descripcion-corta
  ```

- [ ] Añadir la entrada en `CHANGELOG.md`, indicando si hace falta algo manual en las tiendas:
  ```markdown
  ## v1.1.0 – Descripción
  - Qué cambia.
  - ⚠️ Requiere nueva variable `XXX` en `.env`.   ← si aplica
  ```

- [ ] Publicar la versión:
  ```bash
  git commit -am "Release v1.1.0"
  git tag v1.1.0
  git push origin master --tags
  ```

**Qué número de versión usar:**

| Tipo de cambio | Ejemplo | Versión |
|---|---|---|
| Arreglo de un bug | `v1.1.0` → `v1.1.1` | PATCH |
| Funcionalidad nueva compatible | `v1.1.0` → `v1.2.0` | MINOR |
| Cambio que obliga a tocar las tiendas (API, BD, config) | `v1.2.0` → `v2.0.0` | MAJOR |

### 2.2 En cada tienda activa (solo las que quieras actualizar)

- [ ] Traer la versión:
  ```bash
  cd TiendaX-API
  git checkout master && git pull
  git fetch upstream --tags
  git merge v1.1.0
  ```

- [ ] **Si hay conflictos:**
  ```bash
  git status                 # muestra los ficheros en conflicto
  # editar cada fichero, quedarse con lo correcto y quitar las marcas <<<<<<< ======= >>>>>>>
  git add .
  git commit                 # cierra el merge
  ```
  Para cancelar el merge y volver a como estaba: `git merge --abort`.

- [ ] Revisar el `CHANGELOG.md`: si pide variables nuevas, añádelas al `.env` de la tienda.

- [ ] Probar y subir:
  ```bash
  mvn test
  git push
  ```

- [ ] Actualizar en el README de la tienda: `> Basada en GenericAPI v1.1.0`.

---

## Paso 3 · Congelar una tienda terminada

Para tiendas en producción que ya no van a recibir cambios del genérico.

- [ ] Ver en qué versión se queda:
  ```bash
  cd TiendaVieja-API
  git describe --tags
  ```

- [ ] Desconectarla del genérico para que no se pueda hacer un merge por error:
  ```bash
  git remote remove upstream
  ```

- [ ] En su README:
  ```markdown
  > ❄️ Congelada en GenericAPI vX.Y.Z – en producción, sin mantenimiento activo.
  ```
  ```bash
  git commit -am "Freeze at GenericAPI vX.Y.Z"
  git push
  ```

- [ ] (Opcional) GitHub → repo de la tienda → *Settings → General → Danger Zone → **Archive this repository***. Queda en solo lectura; se puede desarchivar en cualquier momento.

- [ ] Marcarla como ❄️ Congelada en el [Registro de tiendas](#registro-de-tiendas).

---

## Paso 4 · Parche urgente a una tienda congelada

Para un fallo grave (por ejemplo de seguridad) que ya está arreglado en el genérico. Se trae **solo ese commit**, no las versiones nuevas.

- [ ] Si el repo está archivado: GitHub → *Settings → Unarchive this repository*.

- [ ] Localizar el hash del arreglo en el genérico:
  ```bash
  cd GenericAPI-REST
  git log --oneline          # copia el hash del commit del fix
  ```

- [ ] Aplicarlo en la tienda:
  ```bash
  cd TiendaVieja-API
  git remote add upstream git@github.com:ElMostWantedK/GenericAPI.git
  git remote set-url --push upstream NO_PUSH
  git fetch upstream
  git cherry-pick <hash>
  # si hay conflictos: resolver → git add . → git cherry-pick --continue
  mvn test
  git push
  ```

- [ ] Volver a congelarla:
  ```bash
  git remote remove upstream
  ```
  Y volver a archivar el repo en GitHub si lo estaba.

- [ ] Desplegar en producción.

> Conviene dejar acordado con el cliente si este tipo de parches entran o no en el mantenimiento.

---

## Paso 5 · Llevar al genérico un arreglo hecho en una tienda

Si al trabajar en una tienda arreglas algo que **también está en el genérico**, no lo dejes solo en esa tienda.

- [ ] Localizar el hash del commit en la tienda (`git log --oneline` dentro de `TiendaX-API`).

- [ ] Traerlo al genérico:
  ```bash
  cd GenericAPI-REST
  git remote add tiendax git@github.com:ElMostWantedK/TiendaX-API.git   # solo la primera vez
  git fetch tiendax
  git checkout -b fix/descripcion
  git cherry-pick <hash>
  mvn test
  ```
  Comprueba que el commit **no incluya nada propio de la tienda**. Si lo incluye, es mejor rehacer el arreglo a mano en el genérico.

- [ ] Publicar una nueva versión y llevarla a las demás tiendas activas ([Paso 2](#paso-2--llevar-un-cambio-del-genérico-a-las-tiendas-activas)).

---

## Reglas para evitar conflictos

1. **Los cambios comunes se hacen en el genérico, no en las tiendas.** Una tienda nunca hace `push` a `upstream` (ya está bloqueado con `NO_PUSH`).
2. **El código propio de cada tienda va en ficheros o paquetes nuevos** (por ejemplo `com.generic.rest.main.tiendax`). Cuanto menos edites los ficheros del genérico dentro de una tienda, menos conflictos habrá al hacer el merge.
3. **Lo que cambia entre tiendas va en configuración** (`.env` / properties), no escrito en el código.
4. **Migraciones de Liquibase propias de una tienda:** en su propia carpeta (`db/changelog/tiendax/`) y con ids con prefijo (`tiendax-0001-...`). Nunca modifiques los changesets del genérico.
5. **Secrets distintos en cada tienda:** JWT, base de datos y cliente de Google OAuth. Con el mismo `JWT_SECRET`, un token de una tienda sería válido en otra.
6. **Pasa siempre `mvn test` antes de hacer push** tras un merge.

---

## Chuleta

| Quiero… | Dónde | Comandos |
|---|---|---|
| Crear tienda | nuevo | `clone` → `remote rename origin upstream` → `remote add origin …` → `set-url --push upstream NO_PUSH` → `push` |
| Publicar un cambio común | genérico | commit → CHANGELOG → `tag vX.Y.Z` → `push --tags` |
| Actualizar tienda activa | tienda | `fetch upstream --tags` → `merge vX.Y.Z` → `mvn test` → `push` |
| Ver versión de una tienda | tienda | `git describe --tags` |
| Congelar tienda | tienda | `remote remove upstream` + archivar en GitHub |
| Parche a tienda congelada | tienda | `remote add upstream …` → `cherry-pick <hash>` → `push` → `remote remove upstream` |
| Arreglo de tienda → genérico | genérico | `remote add tiendax …` → `cherry-pick <hash>` → tag |
| Cancelar un merge con conflictos | tienda | `git merge --abort` |

---

## Registro de tiendas

Mantén esta tabla al día en el genérico para saber qué tiendas existen y en qué versión está cada una.

| Tienda | Repo | Estado | Versión del genérico | Notas |
|---|---|---|---|---|
| _Ejemplo_ | `ElMostWantedK/TiendaX-API` | 🟢 Activa | v1.0.0 | — |

Estados: 🟢 Activa (recibe merges) · ❄️ Congelada (solo parches puntuales) · 🗄️ Archivada
