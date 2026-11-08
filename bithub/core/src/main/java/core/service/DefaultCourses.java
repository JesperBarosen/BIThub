package core.service;

import core.model.Course;
import java.util.List;

/**
 * Standard emner som alltid skal finnes i BIThub.
 */
public final class DefaultCourses {

    private DefaultCourses() {
    }

    // Laget med hjelp av KI, gjennomgått og tilpasset av gruppen.
    /**
     * Lager standardemnene uten anmeldelser.
     *
     * @return en ny liste med standardemnene
     */
    public static List<Course> all() {
        return List.of(new Course("BI2081", "Natur, miljø og bærekraft"),
                new Course("EXPH0300", "Examen philosophicum for naturvitenskap og teknologi"),
                new Course("HMS0002", "HMS-kurs for 1. årsstudenter"),
                new Course("IT1604", "Digitalt samfunn"),
                new Course("IT1901", "Informatikk prosjektarbeid I"),
                new Course("IT2805", "Webteknologi"), new Course("IT2810", "Webutvikling"),
                new Course("IT2901", "Informatikk prosjektarbeid II"),
                new Course("IT3212", "Datadrevet Programvare"),
                new Course("IØ2000",
                        "Hvordan bli en endringsagent? Innovasjon og entreprenørskap i praksis"),
                new Course("MA0001", "Brukerkurs i matematikk A"),
                new Course("MA0301", "Elementær diskret matematikk"),
                new Course("TDT4100", "Objektorientert programmering"),
                new Course("TDT4109", "Informasjonsteknologi, grunnkurs"),
                new Course("TDT4117", "Informasjonsgjenfinning"),
                new Course("TDT4120", "Algoritmer og datastrukturer"),
                new Course("TDT4136", "Introduksjon til kunstig intelligens"),
                new Course("TDT4137", "Kognitive systemer"),
                new Course("TDT4140", "Programvareutvikling"),
                new Course("TDT4145", "Datamodellering og databasesystemer"),
                new Course("TDT4160", "Datamaskiner"), new Course("TDT4165", "Programmeringsspråk"),
                new Course("TDT4171", "Metoder i kunstig intelligens"),
                new Course("TDT4175", "Informasjonssystemer"),
                new Course("TDT4180", "Menneske-maskin-interaksjon"),
                new Course("TDT4186", "Operativsystemer"),
                new Course("TDT4205", "Kompilatorteknikk"),
                new Course("TDT4237", "Programvaresikkerhet og personvern"),
                new Course("TDT4240", "Programvarearkitektur"),
                new Course("TDT4258", "Maskinnær programmering"),
                new Course("TDT4300", "Datavarehus og datagruvedrift"),
                new Course("TDT4310", "Intelligent tekstanalyse og språkforståelse"),
                new Course("TTM4100", "Kommunikasjon - Tjenester og nett"),
                new Course("TTM4135", "Anvendt kryptografi og nettverksikkerhet"),
                new Course("TTM4185", "Sikkerhet og robusthet i IKT system"),
                new Course("ØKO1001", "Ledelse"));
    }
}