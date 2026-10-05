package com.debuggeandoideas.orion_resource_server.store;

import com.debuggeandoideas.orion_resource_server.model.CrewMember;
import com.debuggeandoideas.orion_resource_server.model.Mission;
import com.debuggeandoideas.orion_resource_server.model.News;
import jakarta.annotation.PostConstruct;
import net.datafaker.Faker;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Random;

@Component
public class MissionControlStore {

    private static final long SEED = 2087L;
    private static final int NEWS_COUNT = 5;
    private static final int MISSION_COUNT = 4;
    private static final int FIRST_NEWS_NUMBER = 101;
    private static final int MIN_CREW = 2;
    private static final int MAX_CREW = 4;
    private static final int MIN_FINDINGS = 1;
    private static final int MAX_FINDINGS = 3;
    private static final LocalDate FIRST_DAY = LocalDate.of(2087, 1, 1);
    private static final LocalDate LAST_DAY = LocalDate.of(2087, 12, 31);

    private static final List<String> NEWS_TAGS = List.of(
            "COMUNICADO", "CIENCIA", "TRIPULACIÓN", "TECNOLOGÍA", "AVISO");

    private static final List<String> HEADLINES = List.of(
            "Detectan señal repetitiva cerca de Europa",
            "El comedor de la estación estrena menú marciano",
            "Un OVNI se estaciona en doble fila frente al hangar 3",
            "La antena principal vuelve a captar reguetón de origen desconocido",
            "Se pierde un dron explorador y regresa con una calcomanía",
            "Nuevo protocolo de saludo para visitantes de otros planetas",
            "El telescopio Orión fotografía algo que nos saluda de vuelta",
            "Simulacro de primer contacto termina en partida de dominó");

    private static final List<String> NEWS_SUMMARIES = List.of(
            "El equipo científico pide calma. El equipo de cocina pide refuerzos.",
            "Los analistas llevan tres noches sin dormir y ya le pusieron nombre.",
            "Control de misión recuerda que no se debe alimentar a las anomalías.",
            "Nadie sabe quién lo autorizó, pero todos coinciden en que funciona.",
            "El informe oficial tiene doce páginas; once están tachadas con marcador negro.",
            "Se investigan las causas. Mientras tanto, favor de no tocar el botón rojo.",
            "La dirección asegura que todo está bajo control, aunque lo dijo sonriendo raro.",
            "Los testigos describen luces, un zumbido y un ligero olor a canela.");

    private static final List<String> CODENAMES = List.of(
            "Luna Roja", "Señal de Europa", "Cinturón Silencio", "Operación Eco",
            "Faro Oscuro", "Polvo de Estrella", "Visitante Nocturno", "Órbita Fantasma");

    private static final List<String> DESTINATIONS = List.of(
            "Fobos", "Europa", "Cinturón de Kuiper", "Titán",
            "Encélado", "Ceres", "Cara oculta de la Luna", "Ganímedes");

    private static final List<String> STATUSES = List.of("ACTIVA", "EN ANÁLISIS", "CONCLUIDA");

    private static final List<String> CLASSIFICATIONS = List.of("ULTRA SECRETO", "SECRETO", "CONFIDENCIAL");

    private static final List<String> MISSION_SUMMARIES = List.of(
            "Investigar una estructura metálica que no aparece en ningún mapa y que, al parecer, cambia de lugar.",
            "Rastrear el origen de una transmisión que repite la misma melodía cada 47 minutos.",
            "Recuperar una sonda que dejó de responder justo después de enviar una foto borrosa.",
            "Verificar si las luces observadas bajo el hielo son naturales o alguien dejó algo encendido.",
            "Establecer contacto con una nave que lleva tres semanas estacionada sin decir nada.",
            "Analizar un objeto que orbita en sentido contrario y parece disfrutarlo.");

    private static final List<String> SPECIALTIES = List.of(
            "Xenobiología", "Navegación", "Criptolingüística", "Astrofísica",
            "Ingeniería de propulsión", "Medicina espacial", "Comunicaciones", "Geología planetaria");

    private static final List<String> FINDINGS = List.of(
            "Huellas de tres dedos alrededor del módulo de aterrizaje.",
            "Una señal de radio que responde cuando se le habla en español.",
            "Restos de una aleación que no figura en la tabla periódica.",
            "Un símbolo tallado en la roca idéntico al logotipo de la misión.",
            "Fluctuaciones de gravedad cada vez que alguien cuenta un chiste.",
            "Un objeto esférico tibio que ronronea al acercarse.",
            "Marcas de aterrizaje demasiado ordenadas para ser casuales.",
            "Un cristal que emite luz verde solo los martes.",
            "Muestras de hielo con burbujas que forman patrones repetidos.");

    private static final List<String> CLASSIFIED_NOTES = List.of(
            "El visitante pidió hablar con nuestro líder. Le pasamos al de sistemas.",
            "No mencionar el incidente del café en presencia del embajador de Titán.",
            "La tripulación jura que la roca les guiñó. Se recomienda descanso.",
            "Si el objeto empieza a cantar, abandonar el módulo con dignidad.",
            "El traductor automático insiste en que nos llamaron 'vecinos ruidosos'.",
            "Bajo ninguna circunstancia aceptar otra invitación a cenar.");

    private List<News> news = List.of();
    private List<Mission> missions = List.of();

    @PostConstruct
    public void generateData() {
        Random random = new Random(SEED);
        Faker faker = new Faker(Locale.of("es"), random);
        news = generateNews(random);
        missions = generateMissions(random, faker);
    }

    public List<News> findAllNews() {
        return news.stream().sorted(Comparator.comparing(News::id)).toList();
    }

    public List<Mission> findAllMissions() {
        return missions.stream().sorted(Comparator.comparing(Mission::id)).toList();
    }

    private List<News> generateNews(Random random) {
        List<String> headlines = pickDistinct(HEADLINES, NEWS_COUNT, random);
        List<String> summaries = pickDistinct(NEWS_SUMMARIES, NEWS_COUNT, random);
        List<News> generated = new ArrayList<>();
        for (int index = 0; index < NEWS_COUNT; index++) {
            generated.add(new News(
                    "N-" + (FIRST_NEWS_NUMBER + index),
                    randomDate(random),
                    pickOne(NEWS_TAGS, random),
                    headlines.get(index),
                    summaries.get(index)));
        }
        return List.copyOf(generated);
    }

    private List<Mission> generateMissions(Random random, Faker faker) {
        List<String> codenames = pickDistinct(CODENAMES, MISSION_COUNT, random);
        List<String> destinations = pickDistinct(DESTINATIONS, MISSION_COUNT, random);
        List<String> summaries = pickDistinct(MISSION_SUMMARIES, MISSION_COUNT, random);
        List<String> classifiedNotes = pickDistinct(CLASSIFIED_NOTES, MISSION_COUNT, random);
        List<Mission> generated = new ArrayList<>();
        for (int index = 0; index < MISSION_COUNT; index++) {
            generated.add(new Mission(
                    "M-%03d".formatted(index + 1),
                    codenames.get(index),
                    destinations.get(index),
                    pickOne(STATUSES, random),
                    pickOne(CLASSIFICATIONS, random),
                    randomDate(random),
                    summaries.get(index),
                    generateCrew(random, faker),
                    pickDistinct(FINDINGS, randomBetween(MIN_FINDINGS, MAX_FINDINGS, random), random),
                    classifiedNotes.get(index)));
        }
        return List.copyOf(generated);
    }

    private List<CrewMember> generateCrew(Random random, Faker faker) {
        int crewSize = randomBetween(MIN_CREW, MAX_CREW, random);
        List<String> specialties = pickDistinct(SPECIALTIES, crewSize, random);
        return specialties.stream()
                .map(specialty -> new CrewMember(faker.name().fullName(), specialty))
                .toList();
    }

    private static List<String> pickDistinct(List<String> pool, int count, Random random) {
        List<String> shuffled = new ArrayList<>(pool);
        Collections.shuffle(shuffled, random);
        return List.copyOf(shuffled.subList(0, count));
    }

    private static String pickOne(List<String> pool, Random random) {
        return pool.get(random.nextInt(pool.size()));
    }

    private static int randomBetween(int minInclusive, int maxInclusive, Random random) {
        return minInclusive + random.nextInt(maxInclusive - minInclusive + 1);
    }

    private static String randomDate(Random random) {
        long totalDays = ChronoUnit.DAYS.between(FIRST_DAY, LAST_DAY) + 1;
        return FIRST_DAY.plusDays(random.nextInt((int) totalDays)).toString();
    }
}
