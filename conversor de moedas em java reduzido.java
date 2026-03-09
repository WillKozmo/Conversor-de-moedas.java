import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Scanner;

public class ConversorMoedas {
    
    private static final String API_KEY = "a313070ada56e87f54b6859b";
    private static final String BASE_URL = "https://v6.exchangerate-api.com/v6/";
    
    private final HttpClient httpClient;
    private final Scanner scanner;
    
    public ConversorMoedas() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.scanner = new Scanner(System.in);
    }
    
    public static void main(String[] args) {
        new ConversorMoedas().iniciar();
    }
    
    public void iniciar() {
        boolean executando = true;
        
        while (executando) {
            exibirMenu();
            int opcao = lerOpcao();
            
            switch (opcao) {
                case 1 -> converter("USD", "ARS");
                case 2 -> converter("ARS", "USD");
                case 3 -> converter("USD", "BRL");
                case 4 -> converter("BRL", "USD");
                case 5 -> converter("USD", "COP");
                case 6 -> converter("COP", "USD");
                case 7 -> converterPersonalizado();
                case 0 -> {
                    executando = false;
                    System.out.println("Obrigado por usar o Conversor!");
                }
                default -> System.out.println("Opcao invalida!");
            }
        }
        
        scanner.close();
    }
    
    private void exibirMenu() {
        System.out.println("\n=== CONVERSOR DE MOEDAS ===");
        System.out.println("1. USD -> ARS");
        System.out.println("2. ARS -> USD");
        System.out.println("3. USD -> BRL");
        System.out.println("4. BRL -> USD");
        System.out.println("5. USD -> COP");
        System.out.println("6. COP -> USD");
        System.out.println("7. Conversao Personalizada");
        System.out.println("0. Sair");
        System.out.print("Escolha: ");
    }
    
    private int lerOpcao() {
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
    
    private double lerValor() {
        while (true) {
            System.out.print("Valor: ");
            try {
                return Double.parseDouble(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Valor invalido!");
            }
        }
    }
    
    private void converter(String origem, String destino) {
        double valor = lerValor();
        
        try {
            double taxa = buscarTaxa(origem, destino);
            double resultado = valor * taxa;
            System.out.printf("%.2f %s = %.2f %s (taxa: %.4f)%n", 
                    valor, origem, resultado, destino, taxa);
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }
    
    private void converterPersonalizado() {
        System.out.print("Origem: ");
        String origem = scanner.nextLine().trim().toUpperCase();
        System.out.print("Destino: ");
        String destino = scanner.nextLine().trim().toUpperCase();
        converter(origem, destino);
    }
    
    private double buscarTaxa(String de, String para) throws Exception {
        String url = BASE_URL + API_KEY + "/pair/" + de + "/" + para;
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .timeout(Duration.ofSeconds(10))
                .build();
        
        HttpResponse<String> response = httpClient.send(request, 
                HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() != 200) {
            throw new RuntimeException("HTTP " + response.statusCode());
        }
        
        return extrairTaxa(response.body());
    }
    
    private double extrairTaxa(String json) {
        String key = "\"conversion_rate\":";
        int i = json.indexOf(key);
        if (i == -1) throw new RuntimeException("Taxa nao encontrada");
        
        int start = i + key.length();
        int end = json.indexOf(",", start);
        if (end == -1) end = json.indexOf("}", start);
        
        return Double.parseDouble(json.substring(start, end).trim());
    }
}