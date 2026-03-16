-- ============================================================
-- BASE DE DONNÉES OPTIMALE - AGRICONNECT
-- ============================================================

-- ============================================================
-- 1. RÉFÉRENTIELS
-- ============================================================

CREATE TABLE status (
    id_sta SERIAL PRIMARY KEY,
    libelle VARCHAR(50) UNIQUE NOT NULL,
    icone VARCHAR(100),
    couleur_hex VARCHAR(7)
);

CREATE TABLE zone (
    id_z SERIAL PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    point_depart VARCHAR(255),
    point_arrive VARCHAR(255),
    distance_km DECIMAL(10,2),
    image_carte VARCHAR(500),
    actif BOOLEAN DEFAULT TRUE
);

CREATE TABLE categories_abo (
    id_cat_abo SERIAL PRIMARY KEY,
    libelle VARCHAR(100) NOT NULL,
    duree INTEGER,
    prix NUMERIC(10,2)
);

CREATE TABLE type_alerte (
    id_type SERIAL PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    libelle VARCHAR(255) NOT NULL,
    niveau_urgence INTEGER DEFAULT 1
);

-- ============================================================
-- 2. UTILISATEUR
-- ============================================================

CREATE TABLE utilisateur (
    id SERIAL PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    num_tel VARCHAR(20),
    email VARCHAR(150) UNIQUE NOT NULL,
    mdp VARCHAR(255) NOT NULL,
    photo VARCHAR(500),
    local_habitat VARCHAR(255),
    date_inscript TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_naissance DATE,
    actif BOOLEAN DEFAULT TRUE,
    derniere_connexion TIMESTAMP
);

-- ============================================================
-- 3. HÉRITAGE
-- ============================================================

CREATE TABLE producteur (
    id SERIAL PRIMARY KEY,
    id_utilisateur INTEGER UNIQUE REFERENCES utilisateur(id) ON DELETE CASCADE,
    local_cult VARCHAR(255),
    photo_exploitation VARCHAR(500),
    logo_entreprise VARCHAR(500),
    certification_bio BOOLEAN DEFAULT FALSE,
    site_web VARCHAR(255),
    description_activite TEXT
);

CREATE TABLE transporteur (
    id SERIAL PRIMARY KEY,
    id_utilisateur INTEGER UNIQUE REFERENCES utilisateur(id) ON DELETE CASCADE,
    num_cni VARCHAR(50) UNIQUE NOT NULL,
    permis VARCHAR(50),
    photo_permis VARCHAR(500),
    photo_carte_grise VARCHAR(500),
    experience_annees INTEGER,
    note_moyenne DECIMAL(3,2) DEFAULT 0,
    nb_evaluations INTEGER DEFAULT 0,
    zone_intervention TEXT,
    dispo_immediate BOOLEAN DEFAULT TRUE
);

CREATE TABLE acheteur (
    id SERIAL PRIMARY KEY,
    id_utilisateur INTEGER UNIQUE REFERENCES utilisateur(id) ON DELETE CASCADE,
    local_livraison VARCHAR(255),
    mode_paiement_prefere VARCHAR(50)
);

-- ============================================================
-- 4. VÉHICULES
-- ============================================================

CREATE TABLE voiture (
    id_v SERIAL PRIMARY KEY,
    marque VARCHAR(100) NOT NULL,
    modele VARCHAR(100) NOT NULL,
    immatriculation VARCHAR(20) UNIQUE NOT NULL,
    charge_max DECIMAL(10,2),
    volume_max DECIMAL(10,2),
    photo_voiture VARCHAR(500),
    carte_grise_url VARCHAR(500),
    dat DATE,
    etat VARCHAR(50) DEFAULT 'DISPONIBLE',
    id_transporteur INTEGER REFERENCES transporteur(id)
);

CREATE TABLE maintenance_voiture (
    id_maintenance SERIAL PRIMARY KEY,
    id_voiture INTEGER REFERENCES voiture(id_v) ON DELETE CASCADE,
    date_maintenance DATE NOT NULL,
    type_maintenance VARCHAR(100),
    description TEXT,
    cout DECIMAL(10,2),
    photo_facture VARCHAR(500),
    prochaine_maintenance DATE
);

CREATE TABLE occuper (
    id_transporteur INTEGER REFERENCES transporteur(id),
    id_v INTEGER REFERENCES voiture(id_v),
    da_he DATE,
    PRIMARY KEY (id_transporteur, id_v)
);

-- ============================================================
-- 5. PRODUITS ET COLIS
-- ============================================================

CREATE TABLE produits (
    id_pro SERIAL PRIMARY KEY,
    nom VARCHAR(200) NOT NULL,
    descript TEXT,
    image VARCHAR(500),
    prix_unitaire DECIMAL(10,2),
    poids_unitaire DECIMAL(10,2),
    stock_disponible INTEGER DEFAULT 0,
    date_peremption DATE,
    id_producteur INTEGER REFERENCES producteur(id)
);

CREATE TABLE colis (
    id_c SERIAL PRIMARY KEY,
    code_suivi VARCHAR(50) UNIQUE NOT NULL,
    titre VARCHAR(200),
    descript TEXT,
    photo_colis VARCHAR(500),
    dh_depart TIMESTAMP NOT NULL,
    dh_arrivee_prevue TIMESTAMP,
    dh_arrivee_reelle TIMESTAMP,
    poids_total DECIMAL(10,2),
    volume_total DECIMAL(10,2),
    estimation_prix DECIMAL(10,2),
    prix_final DECIMAL(10,2),
    id_trans INTEGER REFERENCES transporteur(id),
    id_acheteur INTEGER REFERENCES acheteur(id),
    id_z INTEGER REFERENCES zone(id_z),
    id_sta INTEGER REFERENCES status(id_sta)
);

CREATE TABLE con_prod_colis (
    id_c INTEGER REFERENCES colis(id_c) ON DELETE CASCADE,
    id_pro INTEGER REFERENCES produits(id_pro) ON DELETE CASCADE,
    quantite INTEGER NOT NULL,
    prix_unitaire_applique DECIMAL(10,2),
    PRIMARY KEY (id_c, id_pro)
);

-- ============================================================
-- 6. SUIVI GPS
-- ============================================================

CREATE TABLE position (
    id_pos SERIAL PRIMARY KEY,
    latitude DECIMAL(10,8) NOT NULL,
    longitude DECIMAL(11,8) NOT NULL,
    altitude DECIMAL(10,2),
    vitesse DECIMAL(10,2),
    horodatage TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    id_c INTEGER REFERENCES colis(id_c),
    id_v INTEGER REFERENCES voiture(id_v)
);

CREATE TABLE trajet (
    id_trajet SERIAL PRIMARY KEY,
    id_c INTEGER REFERENCES colis(id_c),
    debut_trajet TIMESTAMP NOT NULL,
    fin_trajet TIMESTAMP,
    distance_totale DECIMAL(10,2),
    duree_totale INTEGER,
    statut VARCHAR(50) DEFAULT 'EN_COURS'
);

-- ============================================================
-- 7. MESSAGERIE
-- ============================================================

CREATE TABLE conversation (
    id_co SERIAL PRIMARY KEY,
    id_sender INTEGER REFERENCES utilisateur(id),
    id_receiver INTEGER REFERENCES utilisateur(id),
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    type VARCHAR(20) DEFAULT 'PRIVE'
);

CREATE TABLE messages (
    id_me SERIAL PRIMARY KEY,
    contenu TEXT,
    piece_jointe VARCHAR(500),
    date_create TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    id_co INTEGER REFERENCES conversation(id_co) ON DELETE CASCADE,
    id_sender INTEGER REFERENCES utilisateur(id)
);

-- ============================================================
-- 8. ALERTES
-- ============================================================

CREATE TABLE alert_retard (
    id_alert SERIAL PRIMARY KEY,
    libelle VARCHAR(255),
    description TEXT,
    photo_preuve VARCHAR(500),
    da_he_arr_calculer TIMESTAMP,
    da_he_arr_reel TIMESTAMP,
    retard_minutes INTEGER,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    statut VARCHAR(50) DEFAULT 'NOUVELLE',
    id_type INTEGER REFERENCES type_alerte(id_type),
    id_c INTEGER REFERENCES colis(id_c),
    id_transporteur INTEGER REFERENCES transporteur(id)
);

CREATE TABLE notification (
    id_notification SERIAL PRIMARY KEY,
    titre VARCHAR(255) NOT NULL,
    message TEXT,
    date_envoi TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    lu BOOLEAN DEFAULT FALSE,
    id_utilisateur INTEGER REFERENCES utilisateur(id),
    id_alert INTEGER REFERENCES alert_retard(id_alert)
);

-- ============================================================
-- 9. PUBLICATIONS
-- ============================================================

CREATE TABLE publications (
    id_p SERIAL PRIMARY KEY,
    contenu TEXT,
    image VARCHAR(500),
    date_publication TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    nb_likes INTEGER DEFAULT 0,
    id_posteur INTEGER REFERENCES utilisateur(id)
);

CREATE TABLE commentaire (
    id_commentaire SERIAL PRIMARY KEY,
    contenu TEXT NOT NULL,
    date_commentaire TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    id_p INTEGER REFERENCES publications(id_p) ON DELETE CASCADE,
    id_utilisateur INTEGER REFERENCES utilisateur(id)
);

CREATE TABLE like_publication (
    id_p INTEGER REFERENCES publications(id_p) ON DELETE CASCADE,
    id_utilisateur INTEGER REFERENCES utilisateur(id) ON DELETE CASCADE,
    date_like TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_p, id_utilisateur)
);

-- ============================================================
-- 10. ABONNEMENTS ET PAIEMENTS
-- ============================================================

CREATE TABLE abonnements (
    id_a SERIAL PRIMARY KEY,
    date_abo TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_fin TIMESTAMP,
    statut VARCHAR(50) DEFAULT 'ACTIF',
    id_utilisateur INTEGER REFERENCES utilisateur(id),
    id_cat_abo INTEGER REFERENCES categories_abo(id_cat_abo)
);

CREATE TABLE paiement (
    id_paie SERIAL PRIMARY KEY,
    reference VARCHAR(100) UNIQUE NOT NULL,
    montant NUMERIC(10,2) NOT NULL,
    id_sender INTEGER REFERENCES utilisateur(id),
    id_receiver INTEGER REFERENCES utilisateur(id),
    date_send TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    methode_paiement VARCHAR(50),
    raison VARCHAR(255),
    statut VARCHAR(50) DEFAULT 'EN_ATTENTE',
    id_c INTEGER REFERENCES colis(id_c),
    id_a INTEGER REFERENCES abonnements(id_a)
);

-- ============================================================
-- INDEX
-- ============================================================

CREATE INDEX idx_utilisateur_email ON utilisateur(email);
CREATE INDEX idx_colis_code_suivi ON colis(code_suivi);
CREATE INDEX idx_colis_status ON colis(id_sta);
CREATE INDEX idx_colis_zone ON colis(id_z);
CREATE INDEX idx_colis_transporteur ON colis(id_trans);
CREATE INDEX idx_colis_acheteur ON colis(id_acheteur);
CREATE INDEX idx_position_colis ON position(id_c);
CREATE INDEX idx_messages_conversation ON messages(id_co);
CREATE INDEX idx_alert_colis ON alert_retard(id_c);
CREATE INDEX idx_notification_utilisateur ON notification(id_utilisateur, lu);
CREATE INDEX idx_publication_posteur ON publications(id_posteur);

-- ============================================================
-- DONNÉES DE BASE
-- ============================================================

INSERT INTO status (libelle, icone, couleur_hex) VALUES
    ('EN_ATTENTE', 'hourglass-outline', '#FFC107'),
    ('EN_COURS', 'cube-outline', '#17A2B8'),
    ('LIVRE', 'checkmark-circle-outline', '#28A745'),
    ('ANNULE', 'close-circle-outline', '#DC3545'),
    ('EN_RETARD', 'alert-circle-outline', '#FD7E14');

INSERT INTO type_alerte (code, libelle, niveau_urgence) VALUES
    ('RETARD', 'Retard de livraison', 3),
    ('INCIDENT', 'Incident sur la route', 4),
    ('PANNE', 'Panne véhicule', 5),
    ('INFO', 'Information', 1);

INSERT INTO categories_abo (libelle, duree, prix) VALUES
    ('Gratuit', 30, 0),
    ('Premium', 30, 29.99),
    ('Pro', 30, 99.99);
