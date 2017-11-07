package de.mannodermaus.kommandah.di;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

/**
 * Integration between Architecture Components and Dagger. Shamelessly lifted from Google's sample:
 * https://github.com/googlesamples/android-architecture-components/blob/388a10dd5a814ba6aaa9bf8dee8e7c1c5840b3a5/GithubBrowserSample/app/src/main/java/com/android/example/github/viewmodel/GithubViewModelFactory.java
 */
@Singleton
public class KommandahViewModelFactory implements ViewModelProvider.Factory {
  private final Map<Class<? extends ViewModel>, Provider<ViewModel>> creators;

  @Inject
  public KommandahViewModelFactory(Map<Class<? extends ViewModel>, Provider<ViewModel>> creators) {
    this.creators = creators;
  }

  @SuppressWarnings("unchecked")
  @NonNull
  @Override
  public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
    Provider<? extends ViewModel> creator = creators.get(modelClass);
    if (creator == null) {
      for (Map.Entry<Class<? extends ViewModel>, Provider<ViewModel>> entry : creators.entrySet()) {
        if (modelClass.isAssignableFrom(entry.getKey())) {
          creator = entry.getValue();
          break;
        }
      }
    }
    if (creator == null) {
      throw new IllegalArgumentException("unknown model class " + modelClass);
    }
    try {
      return (T) creator.get();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
