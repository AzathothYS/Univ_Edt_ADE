package com.a.univ_edt_ade.EdTFile;

/**
 * Liste des Json keys utilisées
 *
 * Elles proviennent du script qui a généré le fichier
 */

public final class JsonKeys {

    public static final String SUM = "_sum"; // désigne le checksum (md5) de l'objet (jour, semaine, event)

    // keys pour les informations du fichier (toujours dans le dernier élément de la liste)
    public static final String SOURCE = "source_file"; // le nom du fichier dans ADE dont provient le fichier
    public static final String FILE_SUM = "file_sum"; // checksum du ficher

    // keys communes aux jours et semaines :
    public static final String IMPORTANT = "important"; // nombre d'events marqués comme importants (TP ou CC par ex)
    public static final String HOURS = "hours"; // nombre d'heures du jour ou semaine

    // keys pour les semaines :
    public static final String START = "start"; // début de la semaine YYYY-MM-DD
    public static final String END = "end";     // fin de la semaine   YYYY-MM-DD
    public static final String EMPTYDAYS = "empty_days"; // liste des jours vides (0 pour lundi etc...)
    public static final String WEEK = "week_nb"; // numéro de la semaine

    public static final String DAYS = "days"; // liste des jours, contenant les events et leurs infos

    // keys pour les jours
    public static final String DAY = "day"; // la date : YYYYMMDD
    public static final String EVENTS = "events"; // la liste des events de ce jour

    // keys pour les events
    public static final String isIMPORTANT = "siImportant"; // si l'event est important ou non
    public static final String HOUR = "hour"; // liste des horaires : [heure_début, min_début, heure_fin, min_fin]
    public static final String SALLE = "salle";
    public static final String NAME = "titre"; // le nom de event
    public static final String DESC = "desc"; // description de l'event
    public static final String JOUR = "jour"; // jour de l'event, permet de retrouver le jour et la semaine
}
