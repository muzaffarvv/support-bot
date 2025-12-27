package uz.vv.config

import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import uz.vv.base.BaseRepoImpl

@EnableJpaRepositories(
    basePackages = ["uz.vv"],
    repositoryBaseClass = BaseRepoImpl::class
)
@Configuration
class JpaConfig
