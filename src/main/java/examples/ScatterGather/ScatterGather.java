package examples.ScatterGather;

import java.util.concurrent.*;

public class ScatterGather {

    public static void main(String[] args) throws ExecutionException, InterruptedException {

        try(ExecutorService pool = Executors.newFixedThreadPool(3)){
            CompletableFuture<Integer> async1 = CompletableFuture.supplyAsync(() -> {
                AsyncPriceGetter getter = new AsyncPriceGetter(5, 1000);
                return getter.call();
            }, pool);
            CompletableFuture<Integer> async2 = CompletableFuture.supplyAsync(() -> {
                AsyncPriceGetter getter = new AsyncPriceGetter(10, 2000);
                return getter.call();
            }, pool);
            CompletableFuture<Integer> async3 = CompletableFuture.supplyAsync(() -> {
                AsyncPriceGetter getter = new AsyncPriceGetter(20, 3000);
                return getter.call();
            }, pool);

            CompletableFuture<Void> allTasks = CompletableFuture.allOf(async1, async2, async3);
            try {
                allTasks.get(4, TimeUnit.SECONDS); // Wait for all tasks to complete or timeout
                int finalPrice = async1.get() + async2.get() + async3.get();
                System.out.println("Final price: " + finalPrice);
            } catch (TimeoutException e) {
                System.out.println("Failed to receive all prices in time. Returning no price back");
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    static class AsyncPriceGetter implements Callable<Integer> {

        private final int price;
        private final int timeMs;

        AsyncPriceGetter(int price, int timeMs){
            this.price = price;
            this.timeMs = timeMs;
        }

        @Override
        public Integer call(){
            try {
                Thread.sleep(timeMs);
                System.out.println(Thread.currentThread().getName() + ": Returning price " + price + " after " + timeMs + "ms");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
            return price;
        }
    }
}
