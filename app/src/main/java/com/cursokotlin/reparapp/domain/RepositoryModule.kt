package com.cursokotlin.reparapp.domain
import com.cursokotlin.reparapp.domain.repository.AuthRepository
import com.cursokotlin.reparapp.domain.repository.UserRepository
import com.cursokotlin.reparapp.network.repository.AuthRepositoryImpl
import com.cursokotlin.reparapp.network.repository.UserRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository


}
