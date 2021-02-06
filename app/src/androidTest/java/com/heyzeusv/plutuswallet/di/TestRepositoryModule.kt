package com.heyzeusv.plutuswallet.di

import com.heyzeusv.plutuswallet.data.FakeRepository
import com.heyzeusv.plutuswallet.data.Repository
import dagger.Binds
import dagger.Module
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

/**
 *  Hilt module that will replace RepositoryModule when performing tests in androidTest folder.
 */
@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [RepositoryModule::class]
)
interface TestRepositoryModule {

    // provides FakeRepository for testing
    @Binds @Singleton fun provideFakeRepository(repo: FakeRepository): Repository
}