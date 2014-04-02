/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.largetalk.disruptor;

import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import java.nio.ByteBuffer;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Test {

    public static class LongEvent {

        public long value;

        public void set(long value) {
            this.value = value;
        }
    }

    public static class LongEventFactory implements EventFactory<LongEvent> {

        public LongEvent newInstance() {
            return new LongEvent();
        }
    }

    public static class LongEventHandler implements EventHandler<LongEvent> {
        private long time;
        public LongEventHandler() {
            time = System.currentTimeMillis();
        }

        public void onEvent(LongEvent event, long sequence, boolean endOfBatch) {
            if (event.value % 100000 == 0) {
                System.out.println(event.value);
                System.out.println(System.currentTimeMillis() - time);
            }
            //System.out.println("Event: " + event);
        }
    }

    public static class LongEventProducer {

        private final RingBuffer<LongEvent> ringBuffer;

        public LongEventProducer(RingBuffer<LongEvent> ringBuffer) {
            this.ringBuffer = ringBuffer;
        }

        public void onData(ByteBuffer bb) {
            long sequence = ringBuffer.next();  // Grab the next sequence
            try {
                LongEvent event = ringBuffer.get(sequence); // Get the entry in the Disruptor
                // for the sequence
                event.set(bb.getLong(0));  // Fill with data
            } finally {
                ringBuffer.publish(sequence);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        // Executor that will be used to construct new threads for consumers
        Executor executor = Executors.newFixedThreadPool(6);

        // The factory for the event
        LongEventFactory factory = new LongEventFactory();

        // Specify the size of the ring buffer, must be power of 2.
        int bufferSize = 1024;

        // Construct the Disruptor
        Disruptor<LongEvent> disruptor = new Disruptor(factory, bufferSize, executor);

        // Connect the handler
        disruptor.handleEventsWith(new LongEventHandler());

        // Start the Disruptor, starts all threads running
        disruptor.start();

        // Get the ring buffer from the Disruptor to be used for publishing.
        RingBuffer<LongEvent> ringBuffer = disruptor.getRingBuffer();

        LongEventProducer producer = new LongEventProducer(ringBuffer);

        ByteBuffer bb = ByteBuffer.allocate(8);
        for (long l = 0; true; l++) {
            bb.putLong(0, l);
            producer.onData(bb);
 //           Thread.sleep(1000);
        }
    }
}
