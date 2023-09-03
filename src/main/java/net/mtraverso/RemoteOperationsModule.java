package net.mtraverso;

import com.google.inject.Binder;
import com.google.inject.Module;

import static com.google.inject.Scopes.SINGLETON;
import static io.airlift.jaxrs.JaxrsBinder.jaxrsBinder;

public class RemoteOperationsModule implements Module  {
    @Override
    public void configure(Binder binder) {
        binder.bind(RemoteOperationsManager.class).in(SINGLETON);
        binder.bind(SmsManager.class).in(SINGLETON);
        jaxrsBinder(binder).bind(RemoteOperationsResource.class);
        jaxrsBinder(binder).bind(UiApiResource.class);

        jaxrsBinder(binder).bind(CorsFilter.class);
    }

}
