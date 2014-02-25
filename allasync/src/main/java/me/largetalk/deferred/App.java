/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.largetalk.deferred;

import java.lang.Thread.State;
import org.jdeferred.AlwaysCallback;
import org.jdeferred.Deferred;
import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.ProgressCallback;
import org.jdeferred.Promise;
import org.jdeferred.impl.DeferredObject;

public class App {

    public static void main(String args[]) {

        Deferred deferred = new DeferredObject();
        Promise promise = deferred.promise();
        promise.done(new DoneCallback() {
            public void onDone(Object result) {
                
            }
        }).fail(new FailCallback() {
            public void onFail(Object rejection) {
           
            }
        }).progress(new ProgressCallback() {
            public void onProgress(Object progress) {
     
            }
        }).always(new AlwaysCallback() {
            public void onAlways(Promise.State state, Object d, Object r) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        });
    }
}
