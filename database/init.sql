--
-- PostgreSQL database dump
--

\restrict x2bz3qTao0wbzqvAoHrmEPskF2SPLCXhOG735mjYFOpXCofbatr6D73iftR9hbC

-- Dumped from database version 17.9
-- Dumped by pg_dump version 17.9

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: abonnements; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.abonnements (
    id_a integer NOT NULL,
    date_abo timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    date_fin timestamp without time zone,
    statut character varying(50) DEFAULT 'ACTIF'::character varying,
    id_utilisateur integer,
    id_cat_abo integer
);


--
-- Name: abonnements_id_a_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.abonnements_id_a_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: abonnements_id_a_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.abonnements_id_a_seq OWNED BY public.abonnements.id_a;


--
-- Name: acheteur; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.acheteur (
    id integer NOT NULL,
    id_utilisateur integer,
    local_livraison character varying(255),
    mode_paiement_prefere character varying(50)
);


--
-- Name: acheteur_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.acheteur_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: acheteur_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.acheteur_id_seq OWNED BY public.acheteur.id;


--
-- Name: alert_retard; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.alert_retard (
    id_alert integer NOT NULL,
    libelle character varying(255),
    description text,
    photo_preuve character varying(500),
    da_he_arr_calculer timestamp without time zone,
    da_he_arr_reel timestamp without time zone,
    retard_minutes integer,
    date_creation timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    statut character varying(50) DEFAULT 'NOUVELLE'::character varying,
    id_type integer,
    id_c integer,
    id_transporteur integer
);


--
-- Name: alert_retard_id_alert_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.alert_retard_id_alert_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: alert_retard_id_alert_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.alert_retard_id_alert_seq OWNED BY public.alert_retard.id_alert;


--
-- Name: categories_abo; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.categories_abo (
    id_cat_abo integer NOT NULL,
    libelle character varying(100) NOT NULL,
    duree integer,
    prix numeric(38,2)
);


--
-- Name: categories_abo_id_cat_abo_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.categories_abo_id_cat_abo_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: categories_abo_id_cat_abo_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.categories_abo_id_cat_abo_seq OWNED BY public.categories_abo.id_cat_abo;


--
-- Name: colis; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.colis (
    id_c integer NOT NULL,
    code_suivi character varying(50) NOT NULL,
    titre character varying(200),
    descript text,
    photo_colis character varying(500),
    dh_depart timestamp without time zone NOT NULL,
    dh_arrivee_prevue timestamp without time zone,
    dh_arrivee_reelle timestamp without time zone,
    poids_total numeric(10,2),
    volume_total numeric(10,2),
    estimation_prix numeric(10,2),
    prix_final numeric(10,2),
    id_trans integer,
    id_acheteur integer,
    id_z integer,
    id_sta integer
);


--
-- Name: colis_id_c_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.colis_id_c_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: colis_id_c_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.colis_id_c_seq OWNED BY public.colis.id_c;


--
-- Name: commande_offre; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.commande_offre (
    id_commande integer NOT NULL,
    id_publication integer NOT NULL,
    id_acheteur integer NOT NULL,
    id_producteur integer NOT NULL,
    quantite integer NOT NULL,
    poids_total numeric(10,2),
    volume_total numeric(10,2),
    prix_estime numeric(10,2),
    id_colis integer,
    id_transporteur integer,
    statut character varying(50) DEFAULT 'EN_ATTENTE'::character varying,
    date_commande timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    notes text
);


--
-- Name: commande_offre_id_commande_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.commande_offre_id_commande_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: commande_offre_id_commande_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.commande_offre_id_commande_seq OWNED BY public.commande_offre.id_commande;


--
-- Name: commentaire; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.commentaire (
    id_commentaire integer NOT NULL,
    contenu text NOT NULL,
    date_commentaire timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    id_p integer,
    id_utilisateur integer
);


--
-- Name: commentaire_id_commentaire_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.commentaire_id_commentaire_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: commentaire_id_commentaire_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.commentaire_id_commentaire_seq OWNED BY public.commentaire.id_commentaire;


--
-- Name: con_prod_colis; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.con_prod_colis (
    id_c integer NOT NULL,
    id_pro integer NOT NULL,
    quantite integer NOT NULL,
    prix_unitaire_applique numeric(10,2)
);


--
-- Name: conversation; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.conversation (
    id_co integer NOT NULL,
    id_sender integer,
    id_receiver integer,
    date_creation timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    type character varying(20) DEFAULT 'PRIVE'::character varying,
    archive_receiver boolean,
    archive_sender boolean,
    epingle_receiver boolean,
    epingle_sender boolean,
    supprime_receiver boolean,
    supprime_sender boolean
);


--
-- Name: conversation_id_co_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.conversation_id_co_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: conversation_id_co_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.conversation_id_co_seq OWNED BY public.conversation.id_co;


--
-- Name: like_publication; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.like_publication (
    id_p integer NOT NULL,
    id_utilisateur integer NOT NULL,
    date_like timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: maintenance_voiture; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.maintenance_voiture (
    id_maintenance integer NOT NULL,
    id_voiture integer,
    date_maintenance date NOT NULL,
    type_maintenance character varying(100),
    description text,
    cout numeric(10,2),
    photo_facture character varying(500),
    prochaine_maintenance date
);


--
-- Name: maintenance_voiture_id_maintenance_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.maintenance_voiture_id_maintenance_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: maintenance_voiture_id_maintenance_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.maintenance_voiture_id_maintenance_seq OWNED BY public.maintenance_voiture.id_maintenance;


--
-- Name: messages; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.messages (
    id_me integer NOT NULL,
    contenu text,
    piece_jointe character varying(500),
    date_create timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    id_co integer,
    id_sender integer,
    id_reply integer,
    reactions text,
    statut character varying(20),
    supprime_sender boolean,
    supprime_tous boolean,
    supprime_receiver boolean,
    type_message character varying(20)
);


--
-- Name: messages_id_me_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.messages_id_me_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: messages_id_me_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.messages_id_me_seq OWNED BY public.messages.id_me;


--
-- Name: notification; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.notification (
    id_notification integer NOT NULL,
    titre character varying(255) NOT NULL,
    message text,
    date_envoi timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    lu boolean DEFAULT false,
    id_utilisateur integer,
    id_alert integer,
    type_notif character varying(50) DEFAULT 'GENERAL'::character varying,
    icone character varying(10) DEFAULT '🔔'::character varying,
    lien character varying(255),
    data_json text
);


--
-- Name: notification_id_notification_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.notification_id_notification_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: notification_id_notification_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.notification_id_notification_seq OWNED BY public.notification.id_notification;


--
-- Name: occuper; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.occuper (
    id_transporteur integer NOT NULL,
    id_v integer NOT NULL,
    da_he date
);


--
-- Name: paiement; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.paiement (
    id_paie integer NOT NULL,
    reference character varying(100) NOT NULL,
    montant numeric(10,2) NOT NULL,
    id_sender integer,
    id_receiver integer,
    date_send timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    methode_paiement character varying(50),
    raison character varying(255),
    statut character varying(50) DEFAULT 'EN_ATTENTE'::character varying,
    id_c integer,
    id_a integer
);


--
-- Name: paiement_id_paie_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.paiement_id_paie_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: paiement_id_paie_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.paiement_id_paie_seq OWNED BY public.paiement.id_paie;


--
-- Name: position; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public."position" (
    id_pos integer NOT NULL,
    latitude numeric(10,8) NOT NULL,
    longitude numeric(11,8) NOT NULL,
    altitude numeric(10,2),
    vitesse numeric(10,2),
    horodatage timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    id_c integer,
    id_v integer
);


--
-- Name: position_id_pos_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.position_id_pos_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: position_id_pos_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.position_id_pos_seq OWNED BY public."position".id_pos;


--
-- Name: producteur; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.producteur (
    id integer NOT NULL,
    id_utilisateur integer,
    local_cult character varying(255),
    photo_exploitation character varying(500),
    logo_entreprise character varying(500),
    certification_bio boolean DEFAULT false,
    site_web character varying(255),
    description_activite text
);


--
-- Name: producteur_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.producteur_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: producteur_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.producteur_id_seq OWNED BY public.producteur.id;


--
-- Name: produits; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.produits (
    id_pro integer NOT NULL,
    nom character varying(200) NOT NULL,
    descript text,
    image character varying(500),
    prix_unitaire numeric(10,2),
    poids_unitaire numeric(10,2),
    stock_disponible integer DEFAULT 0,
    date_peremption date,
    id_producteur integer
);


--
-- Name: produits_id_pro_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.produits_id_pro_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: produits_id_pro_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.produits_id_pro_seq OWNED BY public.produits.id_pro;


--
-- Name: publications; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.publications (
    id_p integer NOT NULL,
    contenu text,
    image character varying(500),
    date_publication timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    nb_likes integer DEFAULT 0,
    id_posteur integer,
    type_pub character varying(20) DEFAULT 'POST'::character varying,
    id_produit integer,
    quantite_offerte integer,
    prix_offre numeric(10,2),
    unite_mesure character varying(20) DEFAULT 'kg'::character varying,
    disponible boolean DEFAULT true
);


--
-- Name: publications_id_p_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.publications_id_p_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: publications_id_p_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.publications_id_p_seq OWNED BY public.publications.id_p;


--
-- Name: status; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.status (
    id_sta integer NOT NULL,
    libelle character varying(50) NOT NULL,
    icone character varying(100),
    couleur_hex character varying(7)
);


--
-- Name: status_id_sta_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.status_id_sta_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: status_id_sta_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.status_id_sta_seq OWNED BY public.status.id_sta;


--
-- Name: trajet; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.trajet (
    id_trajet integer NOT NULL,
    id_c integer,
    debut_trajet timestamp without time zone NOT NULL,
    fin_trajet timestamp without time zone,
    distance_totale numeric(10,2),
    duree_totale integer,
    statut character varying(50) DEFAULT 'EN_COURS'::character varying
);


--
-- Name: trajet_id_trajet_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.trajet_id_trajet_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: trajet_id_trajet_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.trajet_id_trajet_seq OWNED BY public.trajet.id_trajet;


--
-- Name: transporteur; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.transporteur (
    id integer NOT NULL,
    id_utilisateur integer,
    num_cni character varying(50) NOT NULL,
    permis character varying(50),
    photo_permis character varying(500),
    photo_carte_grise character varying(500),
    experience_annees integer,
    note_moyenne numeric(3,2) DEFAULT 0,
    nb_evaluations integer DEFAULT 0,
    zone_intervention text,
    dispo_immediate boolean DEFAULT true
);


--
-- Name: transporteur_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.transporteur_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: transporteur_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.transporteur_id_seq OWNED BY public.transporteur.id;


--
-- Name: type_alerte; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.type_alerte (
    id_type integer NOT NULL,
    code character varying(50) NOT NULL,
    libelle character varying(255) NOT NULL,
    niveau_urgence integer DEFAULT 1
);


--
-- Name: type_alerte_id_type_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.type_alerte_id_type_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: type_alerte_id_type_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.type_alerte_id_type_seq OWNED BY public.type_alerte.id_type;


--
-- Name: user_fcm_tokens; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.user_fcm_tokens (
    id_utilisateur integer NOT NULL,
    token character varying(500) NOT NULL,
    date_maj timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: utilisateur; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.utilisateur (
    id integer NOT NULL,
    nom character varying(100) NOT NULL,
    prenom character varying(100) NOT NULL,
    num_tel character varying(20),
    email character varying(150) NOT NULL,
    mdp character varying(255) NOT NULL,
    photo character varying(500),
    local_habitat character varying(255),
    date_inscript timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    date_naissance date,
    actif boolean DEFAULT true,
    derniere_connexion timestamp without time zone,
    role character varying(20) NOT NULL,
    latitude numeric(10,8),
    longitude numeric(11,8),
    CONSTRAINT utilisateur_role_check CHECK (((role)::text = ANY ((ARRAY['PRODUCTEUR'::character varying, 'TRANSPORTEUR'::character varying, 'ACHETEUR'::character varying])::text[])))
);


--
-- Name: utilisateur_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.utilisateur_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: utilisateur_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.utilisateur_id_seq OWNED BY public.utilisateur.id;


--
-- Name: voiture; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.voiture (
    id_v integer NOT NULL,
    marque character varying(100) NOT NULL,
    modele character varying(100) NOT NULL,
    immatriculation character varying(20) NOT NULL,
    charge_max numeric(10,2),
    volume_max numeric(10,2),
    photo_voiture character varying(500),
    carte_grise_url character varying(500),
    dat date,
    etat character varying(50) DEFAULT 'DISPONIBLE'::character varying,
    id_transporteur integer,
    lat_actuelle numeric(10,8),
    lon_actuelle numeric(11,8)
);


--
-- Name: voiture_id_v_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.voiture_id_v_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: voiture_id_v_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.voiture_id_v_seq OWNED BY public.voiture.id_v;


--
-- Name: zone; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.zone (
    id_z integer NOT NULL,
    nom character varying(100) NOT NULL,
    point_depart character varying(255),
    point_arrive character varying(255),
    distance_km numeric(10,2),
    image_carte character varying(500),
    actif boolean DEFAULT true
);


--
-- Name: zone_id_z_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.zone_id_z_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: zone_id_z_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.zone_id_z_seq OWNED BY public.zone.id_z;


--
-- Name: abonnements id_a; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.abonnements ALTER COLUMN id_a SET DEFAULT nextval('public.abonnements_id_a_seq'::regclass);


--
-- Name: acheteur id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.acheteur ALTER COLUMN id SET DEFAULT nextval('public.acheteur_id_seq'::regclass);


--
-- Name: alert_retard id_alert; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.alert_retard ALTER COLUMN id_alert SET DEFAULT nextval('public.alert_retard_id_alert_seq'::regclass);


--
-- Name: categories_abo id_cat_abo; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.categories_abo ALTER COLUMN id_cat_abo SET DEFAULT nextval('public.categories_abo_id_cat_abo_seq'::regclass);


--
-- Name: colis id_c; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.colis ALTER COLUMN id_c SET DEFAULT nextval('public.colis_id_c_seq'::regclass);


--
-- Name: commande_offre id_commande; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.commande_offre ALTER COLUMN id_commande SET DEFAULT nextval('public.commande_offre_id_commande_seq'::regclass);


--
-- Name: commentaire id_commentaire; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.commentaire ALTER COLUMN id_commentaire SET DEFAULT nextval('public.commentaire_id_commentaire_seq'::regclass);


--
-- Name: conversation id_co; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.conversation ALTER COLUMN id_co SET DEFAULT nextval('public.conversation_id_co_seq'::regclass);


--
-- Name: maintenance_voiture id_maintenance; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.maintenance_voiture ALTER COLUMN id_maintenance SET DEFAULT nextval('public.maintenance_voiture_id_maintenance_seq'::regclass);


--
-- Name: messages id_me; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.messages ALTER COLUMN id_me SET DEFAULT nextval('public.messages_id_me_seq'::regclass);


--
-- Name: notification id_notification; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.notification ALTER COLUMN id_notification SET DEFAULT nextval('public.notification_id_notification_seq'::regclass);


--
-- Name: paiement id_paie; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.paiement ALTER COLUMN id_paie SET DEFAULT nextval('public.paiement_id_paie_seq'::regclass);


--
-- Name: position id_pos; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."position" ALTER COLUMN id_pos SET DEFAULT nextval('public.position_id_pos_seq'::regclass);


--
-- Name: producteur id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.producteur ALTER COLUMN id SET DEFAULT nextval('public.producteur_id_seq'::regclass);


--
-- Name: produits id_pro; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.produits ALTER COLUMN id_pro SET DEFAULT nextval('public.produits_id_pro_seq'::regclass);


--
-- Name: publications id_p; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.publications ALTER COLUMN id_p SET DEFAULT nextval('public.publications_id_p_seq'::regclass);


--
-- Name: status id_sta; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.status ALTER COLUMN id_sta SET DEFAULT nextval('public.status_id_sta_seq'::regclass);


--
-- Name: trajet id_trajet; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.trajet ALTER COLUMN id_trajet SET DEFAULT nextval('public.trajet_id_trajet_seq'::regclass);


--
-- Name: transporteur id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.transporteur ALTER COLUMN id SET DEFAULT nextval('public.transporteur_id_seq'::regclass);


--
-- Name: type_alerte id_type; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.type_alerte ALTER COLUMN id_type SET DEFAULT nextval('public.type_alerte_id_type_seq'::regclass);


--
-- Name: utilisateur id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.utilisateur ALTER COLUMN id SET DEFAULT nextval('public.utilisateur_id_seq'::regclass);


--
-- Name: voiture id_v; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.voiture ALTER COLUMN id_v SET DEFAULT nextval('public.voiture_id_v_seq'::regclass);


--
-- Name: zone id_z; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.zone ALTER COLUMN id_z SET DEFAULT nextval('public.zone_id_z_seq'::regclass);


--
-- Name: abonnements abonnements_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.abonnements
    ADD CONSTRAINT abonnements_pkey PRIMARY KEY (id_a);


--
-- Name: acheteur acheteur_id_utilisateur_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.acheteur
    ADD CONSTRAINT acheteur_id_utilisateur_key UNIQUE (id_utilisateur);


--
-- Name: acheteur acheteur_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.acheteur
    ADD CONSTRAINT acheteur_pkey PRIMARY KEY (id);


--
-- Name: alert_retard alert_retard_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.alert_retard
    ADD CONSTRAINT alert_retard_pkey PRIMARY KEY (id_alert);


--
-- Name: categories_abo categories_abo_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.categories_abo
    ADD CONSTRAINT categories_abo_pkey PRIMARY KEY (id_cat_abo);


--
-- Name: colis colis_code_suivi_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.colis
    ADD CONSTRAINT colis_code_suivi_key UNIQUE (code_suivi);


--
-- Name: colis colis_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.colis
    ADD CONSTRAINT colis_pkey PRIMARY KEY (id_c);


--
-- Name: commande_offre commande_offre_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.commande_offre
    ADD CONSTRAINT commande_offre_pkey PRIMARY KEY (id_commande);


--
-- Name: commentaire commentaire_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.commentaire
    ADD CONSTRAINT commentaire_pkey PRIMARY KEY (id_commentaire);


--
-- Name: con_prod_colis con_prod_colis_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.con_prod_colis
    ADD CONSTRAINT con_prod_colis_pkey PRIMARY KEY (id_c, id_pro);


--
-- Name: conversation conversation_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.conversation
    ADD CONSTRAINT conversation_pkey PRIMARY KEY (id_co);


--
-- Name: like_publication like_publication_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.like_publication
    ADD CONSTRAINT like_publication_pkey PRIMARY KEY (id_p, id_utilisateur);


--
-- Name: maintenance_voiture maintenance_voiture_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.maintenance_voiture
    ADD CONSTRAINT maintenance_voiture_pkey PRIMARY KEY (id_maintenance);


--
-- Name: messages messages_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.messages
    ADD CONSTRAINT messages_pkey PRIMARY KEY (id_me);


--
-- Name: notification notification_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.notification
    ADD CONSTRAINT notification_pkey PRIMARY KEY (id_notification);


--
-- Name: occuper occuper_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occuper
    ADD CONSTRAINT occuper_pkey PRIMARY KEY (id_transporteur, id_v);


--
-- Name: paiement paiement_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.paiement
    ADD CONSTRAINT paiement_pkey PRIMARY KEY (id_paie);


--
-- Name: paiement paiement_reference_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.paiement
    ADD CONSTRAINT paiement_reference_key UNIQUE (reference);


--
-- Name: position position_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."position"
    ADD CONSTRAINT position_pkey PRIMARY KEY (id_pos);


--
-- Name: producteur producteur_id_utilisateur_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.producteur
    ADD CONSTRAINT producteur_id_utilisateur_key UNIQUE (id_utilisateur);


--
-- Name: producteur producteur_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.producteur
    ADD CONSTRAINT producteur_pkey PRIMARY KEY (id);


--
-- Name: produits produits_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.produits
    ADD CONSTRAINT produits_pkey PRIMARY KEY (id_pro);


--
-- Name: publications publications_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.publications
    ADD CONSTRAINT publications_pkey PRIMARY KEY (id_p);


--
-- Name: status status_libelle_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.status
    ADD CONSTRAINT status_libelle_key UNIQUE (libelle);


--
-- Name: status status_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.status
    ADD CONSTRAINT status_pkey PRIMARY KEY (id_sta);


--
-- Name: trajet trajet_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.trajet
    ADD CONSTRAINT trajet_pkey PRIMARY KEY (id_trajet);


--
-- Name: transporteur transporteur_id_utilisateur_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.transporteur
    ADD CONSTRAINT transporteur_id_utilisateur_key UNIQUE (id_utilisateur);


--
-- Name: transporteur transporteur_num_cni_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.transporteur
    ADD CONSTRAINT transporteur_num_cni_key UNIQUE (num_cni);


--
-- Name: transporteur transporteur_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.transporteur
    ADD CONSTRAINT transporteur_pkey PRIMARY KEY (id);


--
-- Name: type_alerte type_alerte_code_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.type_alerte
    ADD CONSTRAINT type_alerte_code_key UNIQUE (code);


--
-- Name: type_alerte type_alerte_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.type_alerte
    ADD CONSTRAINT type_alerte_pkey PRIMARY KEY (id_type);


--
-- Name: user_fcm_tokens user_fcm_tokens_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_fcm_tokens
    ADD CONSTRAINT user_fcm_tokens_pkey PRIMARY KEY (id_utilisateur);


--
-- Name: utilisateur utilisateur_email_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.utilisateur
    ADD CONSTRAINT utilisateur_email_key UNIQUE (email);


--
-- Name: utilisateur utilisateur_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.utilisateur
    ADD CONSTRAINT utilisateur_pkey PRIMARY KEY (id);


--
-- Name: voiture voiture_immatriculation_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.voiture
    ADD CONSTRAINT voiture_immatriculation_key UNIQUE (immatriculation);


--
-- Name: voiture voiture_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.voiture
    ADD CONSTRAINT voiture_pkey PRIMARY KEY (id_v);


--
-- Name: zone zone_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.zone
    ADD CONSTRAINT zone_pkey PRIMARY KEY (id_z);


--
-- Name: idx_alert_colis; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_alert_colis ON public.alert_retard USING btree (id_c);


--
-- Name: idx_colis_acheteur; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_colis_acheteur ON public.colis USING btree (id_acheteur);


--
-- Name: idx_colis_code_suivi; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_colis_code_suivi ON public.colis USING btree (code_suivi);


--
-- Name: idx_colis_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_colis_status ON public.colis USING btree (id_sta);


--
-- Name: idx_colis_transporteur; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_colis_transporteur ON public.colis USING btree (id_trans);


--
-- Name: idx_colis_zone; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_colis_zone ON public.colis USING btree (id_z);


--
-- Name: idx_commande_offre_acheteur; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_commande_offre_acheteur ON public.commande_offre USING btree (id_acheteur);


--
-- Name: idx_commande_offre_producteur; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_commande_offre_producteur ON public.commande_offre USING btree (id_producteur);


--
-- Name: idx_commande_offre_statut; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_commande_offre_statut ON public.commande_offre USING btree (statut);


--
-- Name: idx_messages_conversation; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_messages_conversation ON public.messages USING btree (id_co);


--
-- Name: idx_notification_user_lu; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_notification_user_lu ON public.notification USING btree (id_utilisateur, lu);


--
-- Name: idx_notification_utilisateur; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_notification_utilisateur ON public.notification USING btree (id_utilisateur, lu);


--
-- Name: idx_position_colis; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_position_colis ON public."position" USING btree (id_c);


--
-- Name: idx_publication_posteur; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_publication_posteur ON public.publications USING btree (id_posteur);


--
-- Name: idx_publications_disponible; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_publications_disponible ON public.publications USING btree (disponible) WHERE (disponible = true);


--
-- Name: idx_publications_produit; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_publications_produit ON public.publications USING btree (id_produit);


--
-- Name: idx_publications_type; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_publications_type ON public.publications USING btree (type_pub);


--
-- Name: idx_utilisateur_email; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_utilisateur_email ON public.utilisateur USING btree (email);


--
-- Name: idx_utilisateur_gps; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_utilisateur_gps ON public.utilisateur USING btree (latitude, longitude) WHERE (latitude IS NOT NULL);


--
-- Name: idx_voiture_gps; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_voiture_gps ON public.voiture USING btree (lat_actuelle, lon_actuelle) WHERE (lat_actuelle IS NOT NULL);


--
-- Name: abonnements abonnements_id_cat_abo_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.abonnements
    ADD CONSTRAINT abonnements_id_cat_abo_fkey FOREIGN KEY (id_cat_abo) REFERENCES public.categories_abo(id_cat_abo);


--
-- Name: abonnements abonnements_id_utilisateur_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.abonnements
    ADD CONSTRAINT abonnements_id_utilisateur_fkey FOREIGN KEY (id_utilisateur) REFERENCES public.utilisateur(id);


--
-- Name: acheteur acheteur_id_utilisateur_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.acheteur
    ADD CONSTRAINT acheteur_id_utilisateur_fkey FOREIGN KEY (id_utilisateur) REFERENCES public.utilisateur(id) ON DELETE CASCADE;


--
-- Name: alert_retard alert_retard_id_c_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.alert_retard
    ADD CONSTRAINT alert_retard_id_c_fkey FOREIGN KEY (id_c) REFERENCES public.colis(id_c);


--
-- Name: alert_retard alert_retard_id_transporteur_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.alert_retard
    ADD CONSTRAINT alert_retard_id_transporteur_fkey FOREIGN KEY (id_transporteur) REFERENCES public.transporteur(id);


--
-- Name: alert_retard alert_retard_id_type_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.alert_retard
    ADD CONSTRAINT alert_retard_id_type_fkey FOREIGN KEY (id_type) REFERENCES public.type_alerte(id_type);


--
-- Name: colis colis_id_acheteur_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.colis
    ADD CONSTRAINT colis_id_acheteur_fkey FOREIGN KEY (id_acheteur) REFERENCES public.acheteur(id);


--
-- Name: colis colis_id_sta_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.colis
    ADD CONSTRAINT colis_id_sta_fkey FOREIGN KEY (id_sta) REFERENCES public.status(id_sta);


--
-- Name: colis colis_id_trans_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.colis
    ADD CONSTRAINT colis_id_trans_fkey FOREIGN KEY (id_trans) REFERENCES public.transporteur(id);


--
-- Name: colis colis_id_z_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.colis
    ADD CONSTRAINT colis_id_z_fkey FOREIGN KEY (id_z) REFERENCES public.zone(id_z);


--
-- Name: commande_offre commande_offre_id_acheteur_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.commande_offre
    ADD CONSTRAINT commande_offre_id_acheteur_fkey FOREIGN KEY (id_acheteur) REFERENCES public.acheteur(id);


--
-- Name: commande_offre commande_offre_id_colis_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.commande_offre
    ADD CONSTRAINT commande_offre_id_colis_fkey FOREIGN KEY (id_colis) REFERENCES public.colis(id_c);


--
-- Name: commande_offre commande_offre_id_producteur_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.commande_offre
    ADD CONSTRAINT commande_offre_id_producteur_fkey FOREIGN KEY (id_producteur) REFERENCES public.producteur(id);


--
-- Name: commande_offre commande_offre_id_publication_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.commande_offre
    ADD CONSTRAINT commande_offre_id_publication_fkey FOREIGN KEY (id_publication) REFERENCES public.publications(id_p);


--
-- Name: commande_offre commande_offre_id_transporteur_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.commande_offre
    ADD CONSTRAINT commande_offre_id_transporteur_fkey FOREIGN KEY (id_transporteur) REFERENCES public.transporteur(id);


--
-- Name: commentaire commentaire_id_p_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.commentaire
    ADD CONSTRAINT commentaire_id_p_fkey FOREIGN KEY (id_p) REFERENCES public.publications(id_p) ON DELETE CASCADE;


--
-- Name: commentaire commentaire_id_utilisateur_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.commentaire
    ADD CONSTRAINT commentaire_id_utilisateur_fkey FOREIGN KEY (id_utilisateur) REFERENCES public.utilisateur(id);


--
-- Name: con_prod_colis con_prod_colis_id_c_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.con_prod_colis
    ADD CONSTRAINT con_prod_colis_id_c_fkey FOREIGN KEY (id_c) REFERENCES public.colis(id_c) ON DELETE CASCADE;


--
-- Name: con_prod_colis con_prod_colis_id_pro_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.con_prod_colis
    ADD CONSTRAINT con_prod_colis_id_pro_fkey FOREIGN KEY (id_pro) REFERENCES public.produits(id_pro) ON DELETE CASCADE;


--
-- Name: conversation conversation_id_receiver_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.conversation
    ADD CONSTRAINT conversation_id_receiver_fkey FOREIGN KEY (id_receiver) REFERENCES public.utilisateur(id);


--
-- Name: conversation conversation_id_sender_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.conversation
    ADD CONSTRAINT conversation_id_sender_fkey FOREIGN KEY (id_sender) REFERENCES public.utilisateur(id);


--
-- Name: like_publication like_publication_id_p_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.like_publication
    ADD CONSTRAINT like_publication_id_p_fkey FOREIGN KEY (id_p) REFERENCES public.publications(id_p) ON DELETE CASCADE;


--
-- Name: like_publication like_publication_id_utilisateur_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.like_publication
    ADD CONSTRAINT like_publication_id_utilisateur_fkey FOREIGN KEY (id_utilisateur) REFERENCES public.utilisateur(id) ON DELETE CASCADE;


--
-- Name: maintenance_voiture maintenance_voiture_id_voiture_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.maintenance_voiture
    ADD CONSTRAINT maintenance_voiture_id_voiture_fkey FOREIGN KEY (id_voiture) REFERENCES public.voiture(id_v) ON DELETE CASCADE;


--
-- Name: messages messages_id_co_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.messages
    ADD CONSTRAINT messages_id_co_fkey FOREIGN KEY (id_co) REFERENCES public.conversation(id_co) ON DELETE CASCADE;


--
-- Name: messages messages_id_sender_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.messages
    ADD CONSTRAINT messages_id_sender_fkey FOREIGN KEY (id_sender) REFERENCES public.utilisateur(id);


--
-- Name: notification notification_id_alert_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.notification
    ADD CONSTRAINT notification_id_alert_fkey FOREIGN KEY (id_alert) REFERENCES public.alert_retard(id_alert);


--
-- Name: notification notification_id_utilisateur_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.notification
    ADD CONSTRAINT notification_id_utilisateur_fkey FOREIGN KEY (id_utilisateur) REFERENCES public.utilisateur(id);


--
-- Name: occuper occuper_id_transporteur_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occuper
    ADD CONSTRAINT occuper_id_transporteur_fkey FOREIGN KEY (id_transporteur) REFERENCES public.transporteur(id);


--
-- Name: occuper occuper_id_v_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occuper
    ADD CONSTRAINT occuper_id_v_fkey FOREIGN KEY (id_v) REFERENCES public.voiture(id_v);


--
-- Name: paiement paiement_id_a_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.paiement
    ADD CONSTRAINT paiement_id_a_fkey FOREIGN KEY (id_a) REFERENCES public.abonnements(id_a);


--
-- Name: paiement paiement_id_c_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.paiement
    ADD CONSTRAINT paiement_id_c_fkey FOREIGN KEY (id_c) REFERENCES public.colis(id_c);


--
-- Name: paiement paiement_id_receiver_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.paiement
    ADD CONSTRAINT paiement_id_receiver_fkey FOREIGN KEY (id_receiver) REFERENCES public.utilisateur(id);


--
-- Name: paiement paiement_id_sender_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.paiement
    ADD CONSTRAINT paiement_id_sender_fkey FOREIGN KEY (id_sender) REFERENCES public.utilisateur(id);


--
-- Name: position position_id_c_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."position"
    ADD CONSTRAINT position_id_c_fkey FOREIGN KEY (id_c) REFERENCES public.colis(id_c);


--
-- Name: position position_id_v_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public."position"
    ADD CONSTRAINT position_id_v_fkey FOREIGN KEY (id_v) REFERENCES public.voiture(id_v);


--
-- Name: producteur producteur_id_utilisateur_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.producteur
    ADD CONSTRAINT producteur_id_utilisateur_fkey FOREIGN KEY (id_utilisateur) REFERENCES public.utilisateur(id) ON DELETE CASCADE;


--
-- Name: produits produits_id_producteur_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.produits
    ADD CONSTRAINT produits_id_producteur_fkey FOREIGN KEY (id_producteur) REFERENCES public.producteur(id);


--
-- Name: publications publications_id_posteur_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.publications
    ADD CONSTRAINT publications_id_posteur_fkey FOREIGN KEY (id_posteur) REFERENCES public.utilisateur(id);


--
-- Name: publications publications_id_produit_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.publications
    ADD CONSTRAINT publications_id_produit_fkey FOREIGN KEY (id_produit) REFERENCES public.produits(id_pro);


--
-- Name: trajet trajet_id_c_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.trajet
    ADD CONSTRAINT trajet_id_c_fkey FOREIGN KEY (id_c) REFERENCES public.colis(id_c);


--
-- Name: transporteur transporteur_id_utilisateur_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.transporteur
    ADD CONSTRAINT transporteur_id_utilisateur_fkey FOREIGN KEY (id_utilisateur) REFERENCES public.utilisateur(id) ON DELETE CASCADE;


--
-- Name: user_fcm_tokens user_fcm_tokens_id_utilisateur_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_fcm_tokens
    ADD CONSTRAINT user_fcm_tokens_id_utilisateur_fkey FOREIGN KEY (id_utilisateur) REFERENCES public.utilisateur(id) ON DELETE CASCADE;


--
-- Name: voiture voiture_id_transporteur_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.voiture
    ADD CONSTRAINT voiture_id_transporteur_fkey FOREIGN KEY (id_transporteur) REFERENCES public.transporteur(id);


--
-- PostgreSQL database dump complete
--

-- \unrestrict x2bz3qTao0wbzqvAoHrmEPskF2SPLCXhOG735mjYFOpXCofbatr6D73iftR9hbC

