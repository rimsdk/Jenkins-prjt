pipeline {
    agent {
        docker {
            image 'maven:3.8.5-openjdk-17-slim' // Image Maven avec Java 17
            args '-v /var/run/docker.sock:/var/run/docker.sock'
        }
    }

    environment {
        DOCKER_IMAGE = "rimsdk/banking-app"
        DOCKER_TAG = "latest"
    }

    stages {
        stage('Verify Environment') {
            steps {
                script {
                    echo "Vérification de la configuration..."
                    sh '''
                        java -version || echo "Java n'est pas installé!"
                        mvn --version || echo "Maven n'est pas installé!"
                        docker --version || echo "Docker n'est pas installé ou accessible!"
                    '''
                }
            }
        }

        stage('Build') {
            steps {
                script {
                    echo "Construction de l'application avec Maven..."
                    sh 'mvn clean package -DskipTests'
                }
            }
        }

        stage('Unit Tests') {
            steps {
                script {
                    echo "Exécution des tests unitaires..."
                    sh 'mvn test'
                }
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Install Docker CLI') {
            steps {
                script {
                    echo "Installation de Docker CLI..."
                    sh '''
                        apt-get update
                        apt-get install -y docker.io
                        docker --version
                    '''
                }
            }
        }

        stage('Docker Build & Push') {
            steps {
                script {
                    echo "Construction et publication de l'image Docker..."
                    withCredentials([usernamePassword(
                        credentialsId: 'dockerhub-credentials',
                        usernameVariable: 'DOCKER_USERNAME',
                        passwordVariable: 'DOCKER_PASSWORD'
                    )]) {
                        sh '''
                            set -e
                            docker build -t ${DOCKER_IMAGE}:${DOCKER_TAG} .
                            echo $DOCKER_PASSWORD | docker login -u $DOCKER_USERNAME --password-stdin
                            docker push ${DOCKER_IMAGE}:${DOCKER_TAG}
                        '''
                    }
                }
            }
        }

        stage('Deploy on Remote Server') {
            steps {
                script {
                    echo "Déploiement de l'application sur le serveur distant..."
                    sh '''
                        # Exemple de commande de déploiement
                        ssh user@remote-server "docker pull ${DOCKER_IMAGE}:${DOCKER_TAG} && docker run -d --name banking-app -p 8080:8080 ${DOCKER_IMAGE}:${DOCKER_TAG}"
                    '''
                }
            }
        }
    }

    post {
        always {
            cleanWs()
        }
    }
}
