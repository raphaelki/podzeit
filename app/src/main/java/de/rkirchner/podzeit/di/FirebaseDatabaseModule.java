package de.rkirchner.podzeit.di;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public abstract class FirebaseDatabaseModule {

    final static String PARENT_REFERENCE_KEY = "urls";

    @Singleton
    @Provides
    static FirebaseDatabase provideFirebaseDatabase() {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseDatabase.setPersistenceEnabled(true);
        return firebaseDatabase;
    }

    @Singleton
    @Provides
    static DatabaseReference provideFirebaseReference(FirebaseDatabase firebaseDatabase) {
        return firebaseDatabase.getReference(PARENT_REFERENCE_KEY);
    }
}
