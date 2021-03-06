package completableFuture;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.stream.Collectors;

/**
 * Created by xianpeng.xia
 * on 2020/8/10 10:55 下午
 */
public class Test {

    private static List<Shop> shops = Arrays.asList(
        new Shop("BestPrice"),
        new Shop("LetsSaveBig"),
        new Shop("MyFavoriteShop"),
        new Shop("BuyItAll"),
        new Shop("MyFavoriteShop"),
        new Shop("BuyItAll"),
        new Shop("MyFavoriteShop"),
        new Shop("BuyItAll"),
        new Shop("BuyItAll")
    );

    private final static Executor executor = Executors.newFixedThreadPool(Math.min(shops.size(), 100), new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setDaemon(true);
            return thread;
        }
    });

    public static void main(String[] args) {
        Shop shop = new Shop();

        long start = System.nanoTime();
        Future<Double> futurePrice = shop.getPriceAsync("my favorite product");
        long invocationTime = ((System.nanoTime() - start) / 1_000_000);
        System.out.println("Invocation returned after " + invocationTime + " msecs");

        // doSomethingElse();

        try {
            double price = futurePrice.get();
            System.out.printf("Price is %.2f%n", price);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        long retrievalTime = ((System.nanoTime() - start) / 1_000_000);
        System.out.println("Price returned after " + retrievalTime + " msecs");

        //
        System.out.println("1-----------------------------------------");
        start = System.nanoTime();
        System.out.println(findPriceStream("iphone12"));
        long duration = (System.nanoTime() - start) / 1_000_000;
        System.out.println("Done in " + duration + " msecs");

        System.out.println("2-----------------------------------------");
        start = System.nanoTime();
        System.out.println(findPricesParallelStream("iphone12"));
        duration = (System.nanoTime() - start) / 1_000_000;
        System.out.println("Done in " + duration + " msecs");

        System.out.println("3-----------------------------------------");
        start = System.nanoTime();
        System.out.println(findPricesCompletableFuture("iphone12"));
        duration = (System.nanoTime() - start) / 1_000_000;
        System.out.println("Done in " + duration + " msecs");

        System.out.println("4-----------------------------------------");
        start = System.nanoTime();
        System.out.println(findPricesCompletableFutureExecutor("iphone12"));
        duration = (System.nanoTime() - start) / 1_000_000;
        System.out.println("Done in " + duration + " msecs");


    }

    public static List<String> findPriceStream(String product) {
        return shops.stream()
            .map(shop -> String.format("%s price is %.2f", shop.getName(), shop.getPrice(product)))
            .collect(Collectors.toList());
    }

    public static List<String> findPricesParallelStream(String product) {
        return shops.parallelStream()
            .map(shop -> String.format("%s price is %.2f", shop.getName(), shop.getPrice(product)))
            .collect(Collectors.toList());
    }

    public static List<String> findPricesCompletableFuture(String product) {
        List<CompletableFuture<String>> priceFuture = shops.stream()
            .map(shop -> CompletableFuture.supplyAsync(
                () -> String.format("%s price is %.2f", shop.getName(), shop.getPrice(product))
                )
            ).collect(Collectors.toList());

        return priceFuture.stream()
            .map(CompletableFuture::join)
            .collect(Collectors.toList());
    }

    public static List<String> findPricesCompletableFutureExecutor(String product) {
        List<CompletableFuture<String>> priceFuture = shops.stream()
            .map(shop -> CompletableFuture.supplyAsync(
                () -> String.format("%s price is %.2f", shop.getName(), shop.getPrice(product)),
                executor
                )
            ).collect(Collectors.toList());

        return priceFuture.stream()
            .map(CompletableFuture::join)
            .collect(Collectors.toList());
    }
}
