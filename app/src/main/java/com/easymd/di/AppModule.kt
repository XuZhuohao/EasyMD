package com.easymd.di

import com.easymd.core.data.FileRepository
import com.easymd.core.data.FileRepositoryImpl
import com.easymd.core.export.ExportService
import com.easymd.core.export.ExportServiceImpl
import com.easymd.core.markdown.CommonmarkParser
import com.easymd.core.markdown.MarkdownParser
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds @Singleton
    abstract fun bindFileRepository(impl: FileRepositoryImpl): FileRepository

    @Binds @Singleton
    abstract fun bindMarkdownParser(impl: CommonmarkParser): MarkdownParser

    @Binds @Singleton
    abstract fun bindExportService(impl: ExportServiceImpl): ExportService
}
