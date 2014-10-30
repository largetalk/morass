/*
 * Copyright (c) AdSame Corporation. All rights reserved.
 */
package com.mycompany.allasync.forkjoin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

public class FileSize {
    
    private final static ForkJoinPool forkJoinPool = new ForkJoinPool();
    private static class FileSizeFinder extends RecursiveTask<Long> {
        final File file;
        
        public FileSizeFinder(final File theFile) {
            file = theFile;
        }

        @Override
        protected Long compute() {
            long size = 0;
            if (file.isFile()) {
                size = file.length();
            } else {
                final File[] children = file.listFiles();
                if (children != null) {
                    List<ForkJoinTask<Long>> tasks = new ArrayList<ForkJoinTask<Long>>();
                    for (final File child : children) {
                        if (child.isFile()) {
                            size += child.length();
                        } else {
                            tasks.add(new FileSizeFinder(child));
                        }
                    }
                    
                    for (final ForkJoinTask<Long> task : invokeAll(tasks)) {
                        size += task.join();
                    }
                }
            }
            return size;
        }
    }
    
    public static void main(String args[]) {
        double x = 1.6d;
        x = x * x * x * x;
        System.out.println(x);
        final long start = System.nanoTime();
        final long total = forkJoinPool.invoke(
                new FileSizeFinder(new File("/tmp")));
        final long end = System.nanoTime();
        System.out.println("Total size " + total);
        System.out.println("Time taken " + (end - start)/1.0e9);
    }
}
