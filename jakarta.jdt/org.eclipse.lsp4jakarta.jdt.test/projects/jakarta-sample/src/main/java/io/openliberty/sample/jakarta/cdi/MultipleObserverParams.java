package io.openliberty.sample.jakarta.cdi;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.ObservesAsync;

public class MultipleObserverParams {
    
    public void validSingleObserves(@Observes String event) {
        System.out.println("Event: " + event);
    }
    
    public void validSingleObservesAsync(@ObservesAsync String event) {
        System.out.println("Event: " + event);
    }

    public void invalidTwoObserves(@Observes String event1, @Observes String event2) {
        System.out.println("Events: " + event1 + ", " + event2);
    }

    public void invalidObservesAndObservesAsync(@Observes String event1, @ObservesAsync String event2) {
        System.out.println("Events: " + event1 + ", " + event2);
    }

    public void invalidThreeObserves(@Observes String event1, @ObservesAsync String event2, @ObservesAsync String event3) {
        System.out.println("Events: " + event1 + ", " + event2 + ", " + event3);
    }
}

