# One Piece Theory Evaluator

Application web pour créer, partager et évaluer des théories sur One Piece avec un système d'évaluation IA.

## 🚀 Technologies

### Backend
- **Spring Boot** - Framework Java
- **PostgreSQL** - Base de données
- **JWT** - Authentification
- **Mistral AI** - Évaluation des théories

### Frontend (2 versions disponibles)

#### ⚛️ React (Nouveau - Recommandé)
- **React 19** + **TypeScript**
- **Vite** - Build ultra-rapide
- **Tailwind CSS** + **shadcn/ui** - Design moderne
- **React Router v7** - Navigation
- **Axios** - HTTP client

#### 🅰️ Angular (Legacy)
- **Angular 20**
- **RxJS**
- **Angular Material**

## 📦 Installation

### Prérequis
- Node.js 18+
- Java 17+
- Docker & Docker Compose
- PostgreSQL 15

### Configuration

1. **Créer le fichier `.env`** à la racine :
```env
# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/onepiece
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=yourpassword
POSTGRES_DB=onepiece
POSTGRES_USER=postgres
POSTGRES_PASSWORD=yourpassword

# JWT
SECRETJWTSTRING=your-secret-jwt-key-change-this-in-production

# AI
MISTRAL_API_KEY=your-mistral-api-key
LANGCHAIN_API_KEY=your-langchain-api-key
LANGCHAIN_PROJECT=your-project-name
```

2. **Ajouter les certificats SSL** (si nécessaire)
- `/backend/certs/cert.crt`
- `/frontend/certs/cert.crt`

## 🏃 Démarrage rapide

### Avec Docker (Recommandé)

#### Version React
```bash
docker-compose -f docker-compose-react.yml up -d
```

#### Version Angular (Legacy)
```bash
docker-compose up -d
```

L'application sera disponible sur http://localhost

### Développement local

#### Frontend React
```bash
cd frontend-react
npm install
npm run dev
```
→ http://localhost:3000

#### Frontend Angular
```bash
cd frontend
npm install
npm start
```
→ http://localhost:4200

#### Backend
```bash
cd backend
./mvnw spring-boot:run
```
→ http://localhost:8080

## 📊 Base de données

### Schéma
- **users** (id, username, email, role, password, created_at)
- **theories** (id, title, content, image_url, version_number, ai_score, is_related_to_last_chapter, created_at, updated_at, user_id)
- **theory_versions** (id, theory_id, version_number, content, created_at)
- **comments** (id, content, created_at, updated_at, user_id, theory_id, parent_id)
- **votes** (id, type {UP,DOWN}, created_at, user_id, theory_id)
- **bookmarks** (id, user_id, theory_id, created_at)
- **daily_vote_series** (theory_id, day, up_count, down_count, total_count, up_ratio)

## ✨ Fonctionnalités

- ✅ Authentification JWT avec encryption
- ✅ CRUD complet des théories avec versioning
- ✅ Évaluation IA des théories (0-100%)
- ✅ Système de votes (upvote/downvote)
- ✅ Commentaires hiérarchiques (replies)
- ✅ Bookmarks
- ✅ Upload d'images sécurisé
- ✅ Rendu Markdown
- ✅ Section "Trending Theories"
- ✅ Section "Latest Chapter"
- ✅ Graphiques de votes journaliers
- ✅ Panel d'administration
- ✅ Modération automatique par IA
- ✅ Filtres et recherche
- ✅ Mode dark/light
- ✅ Responsive design

## 🔐 Rôles utilisateurs

- **USER** - Utilisateur standard (créer theories, voter, commenter)
- **MODERATOR** - Modérateur (+ gérer les contenus flaggés)
- **ADMIN** - Administrateur (+ gérer les utilisateurs et rôles)

## 📁 Structure du projet

```
.
├── backend/              # Spring Boot API
├── frontend/             # Angular app (legacy)
├── frontend-react/       # React app (nouveau)
├── ai-proxy/             # Service proxy pour Mistral AI
├── docker-compose.yml    # Configuration Docker (Angular)
└── docker-compose-react.yml  # Configuration Docker (React)
```

## 🚀 Migration Angular → React

La nouvelle version React est disponible dans `frontend-react/`. Voir [MIGRATION.md](frontend-react/MIGRATION.md) pour plus de détails.

### Pourquoi React ?
- ⚡ Build 10x plus rapide avec Vite
- 📦 Bundle optimisé avec code splitting
- 🎨 UI moderne avec shadcn/ui
- 💪 Meilleures performances
- 🔧 Configuration simplifiée

## 📝 API Documentation

Le backend expose une API REST sur `/api` :

- `POST /api/auth/login` - Connexion
- `POST /api/auth/register` - Inscription
- `GET /api/theories` - Liste des théories
- `POST /api/theories/post` - Créer une théorie
- `GET /api/theories/{id}` - Détails d'une théorie
- `POST /api/votes/up` - Upvote
- `POST /api/comments/post` - Commenter
- `POST /api/bookmarks/add` - Bookmark
- Plus d'endpoints dans le code backend

## 🛠️ Développement

### Backend
- Java 17 avec Spring Boot 3.x
- Maven pour la gestion des dépendances
- Flyway pour les migrations DB

### Frontend React
- React 19 avec TypeScript
- Vite pour le build
- Tailwind CSS pour le styling
- shadcn/ui pour les composants

## 📄 License

Projet personnel - One Piece Theory Evaluator © 2025