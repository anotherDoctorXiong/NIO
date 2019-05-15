package JsonServer;

import io.netty.handler.codec.http.HttpMethod;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RequestController {
    private static final Map<String,Route> map=new ConcurrentHashMap<>();
    public static void initMap(){

    }
    private static class Route {
        private HttpMethod method;
        private Object newInstance;

        private Route(){}

        public static Route buildRoute(HttpMethod method, Object newInstance) {
            Route route = new Route();
            route.method = method;
            route.newInstance = newInstance;
            return route;
        }
    }
}
