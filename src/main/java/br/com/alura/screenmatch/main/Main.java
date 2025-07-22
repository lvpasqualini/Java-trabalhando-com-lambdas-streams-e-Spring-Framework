package br.com.alura.screenmatch.main;

import br.com.alura.screenmatch.model.DadosEpisodio;
import br.com.alura.screenmatch.model.DadosSerie;
import br.com.alura.screenmatch.model.DadosTemporada;
import br.com.alura.screenmatch.model.Episodio;
import br.com.alura.screenmatch.service.ConsumoApi;
import br.com.alura.screenmatch.service.ConverteDados;
import org.springframework.format.datetime.DateFormatter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Main {
    private final Scanner sc = new Scanner(System.in);
    private ConsumoApi consumo = new ConsumoApi();
    private ConverteDados conversor = new ConverteDados();
    private List<DadosTemporada> temporadas = new ArrayList<>();

    private final String URL = "http://www.omdbapi.com/?t=" ;
    private final String API_KEY = "&apikey=5e2444e5";

    public void menu() throws RuntimeException {
        int option;
        while (true) {
            System.out.println("Digite o nome de série para busca");
            var nomeSerie = sc.nextLine();
            try {
                var json  = consumo.obterDados(URL + nomeSerie.replace(" ", "+") + API_KEY);
                DadosSerie dados = conversor.obterDados(json, DadosSerie.class);
                System.out.println(dados);
                for (int i = 1; i <=dados.totalTemporadas() ; i++) {
                    json = consumo.obterDados(URL + nomeSerie.replace(" ", "+") + "&season=" + i + API_KEY);
                    DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
                    temporadas.add(dadosTemporada);
                }
                temporadas.forEach(System.out::println);

                temporadas.forEach(t ->
                        t.episodios().forEach(e ->
                                System.out.println(e.titulo() + e.avaliacao())));

                List<DadosEpisodio> dadosEpisodios = temporadas.stream()
                        .flatMap(t -> t.episodios().stream())
                        .collect(Collectors.toList());

                System.out.println("\nTop 5:\n");
                dadosEpisodios.stream()
                        .filter(e -> !e.avaliacao().equalsIgnoreCase("N/A"))
                        .sorted(Comparator.comparing(DadosEpisodio::avaliacao).reversed())
                        .limit(5)
                        .forEach(System.out::println);

                System.out.println("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");

                List<Episodio> episodios = temporadas.stream()
                        .flatMap(t -> t.episodios().stream()
                                .map( e -> new Episodio(t.numero(), e))
                        ).collect(Collectors.toList());

                episodios.forEach(System.out::println);

                System.out.println("A partir de que ano voce deseja ver os episodios?");
                var ano = sc.nextInt();
                sc.nextLine();

                DateTimeFormatter formatador = DateTimeFormatter.ofPattern("dd/MM/yyyy");

                LocalDate dataBusca = LocalDate.of(ano,1,1);
                episodios.stream()
                        .filter( e-> e.getDataLancamento() != null && e.getDataLancamento().isAfter(dataBusca))
                        .forEach(e -> System.out.println(
                                "Temporada:" + e.getTemporada() +
                                        "Episodio:" + e.getTitulo() +
                                        "Data lancamento: " + e.getDataLancamento().format(formatador)
                        ));

            } catch (Exception e) {
                System.out.println("Erro ao pesquisar série: " + e.getMessage());
            }
            System.out.println("--------------------------------------");
            System.out.println("\nDeseja continuar? \n1 - sim\n0 - não");
            option = sc.nextInt();
            sc.nextLine();

            if (option == 0){
                return;
            }
        }
    }
}