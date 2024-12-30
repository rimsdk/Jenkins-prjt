pipeline {
    agent any

    tools {
        maven 'Maven 3.9.6'  // Nom de l'installation Maven configurée dans Jenkins
        jdk 'JDK 17'         // Version de Java
    }

    environment {
        DOCKER_IMAGE = "rimsdk/banking-app"
        DOCKER_TAG = "${BUILD_NUMBER}"  // Utilisation du numéro de build comme tag
        DOCKER_CONFIG = "/tmp/.docker"
        SONAR_PROJECT_KEY = "banking-app"
    }

    stages {
        stage('Checkout') {
            steps {
                // Récupération du code source
                checkout scm
            }
        }

        stage('Build') {
            steps {
                script {
                    // Construction avec Maven
                    sh 'mvn clean package -DskipTests'
                }
            }
        }

        stage('Tests Unitaires') {
            steps {
                script {
                    sh 'mvn test'
                }
            }
            post {
                always {
                    // Publication des résultats des tests
                    junit '**/target/surefire-reports/*.xml'
                    // Publication de la couverture de code
                    jacoco(
                        execPattern: '**/target/jacoco.exec',
                        classPattern: '**/target/classes',
                        sourcePattern: '**/src/main/java'
                    )
                }
            }
        }

        stage('Analyse de Qualité') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    sh """
                        mvn sonar:sonar \
                        -Dsonar.projectKey=${SONAR_PROJECT_KEY} \
                        -Dsonar.java.coveragePlugin=jacoco \
                        -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
                    """
                }
                // Attendre et vérifier la Quality Gate
                timeout(time: 2, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Build & Push Docker Image') {
            steps {
                script {
                    // Construction de l'image Docker
                    docker.build("${DOCKER_IMAGE}:${DOCKER_TAG}")

                    // Connexion et push vers DockerHub
                    withCredentials([usernamePassword(
                        credentialsId: 'dockerhub-credentials',
                        usernameVariable: 'DOCKER_USERNAME',
                        passwordVariable: 'DOCKER_PASSWORD'
                    )]) {
                        sh """
                            mkdir -p ${DOCKER_CONFIG}
                            echo \$DOCKER_PASSWORD | docker login -u \$DOCKER_USERNAME --password-stdin
                            docker push ${DOCKER_IMAGE}:${DOCKER_TAG}
                            docker tag ${DOCKER_IMAGE}:${DOCKER_TAG} ${DOCKER_IMAGE}:latest
                            docker push ${DOCKER_IMAGE}:latest
                        """
                    }
                }
            }
        }

        stage('Déploiement') {
            steps {
                script {
                    // Déploiement sur l'environnement de staging
                    withCredentials([file(credentialsId: 'kubeconfig', variable: 'KUBECONFIG')]) {
                        sh """
                            kubectl --kubeconfig=\$KUBECONFIG set image deployment/banking-app \
                            banking-app=${DOCKER_IMAGE}:${DOCKER_TAG} -n staging
                        """
                    }
                }
            }
        }
    }

    post {
        always {
            // Nettoyage
            cleanWs()
            sh """
                rm -rf ${DOCKER_CONFIG}
                docker rmi ${DOCKER_IMAGE}:${DOCKER_TAG} || true
                docker rmi ${DOCKER_IMAGE}:latest || true
            """
        }
        success {
            // Notifications en cas de succès
            emailext (
                subject: "Pipeline réussi: ${currentBuild.fullDisplayName}",
                body: "Le pipeline s'est terminé avec succès.",
                recipientProviders: [[$class: 'DevelopersRecipientProvider']]
            )
        }
        failure {
            // Notifications en cas d'échec
            emailext (
                subject: "Pipeline échoué: ${currentBuild.fullDisplayName}",
                body: "Le pipeline a échoué. Veuillez vérifier les logs Jenkins.",
                recipientProviders: [[$class: 'DevelopersRecipientProvider']]
            )
        }
    }
}