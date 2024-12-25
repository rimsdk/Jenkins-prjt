pipeline {
    agent {
        docker {
            image 'docker:20.10.24-dind' // Image Docker pour exécuter les commandes Docker
            args '--privileged -v /var/run/docker.sock:/var/run/docker.sock'
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
                        docker --version || echo "Docker n'est pas installé!"
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

        stage('Test') {
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
                            docker build -t ${DOCKER_IMAGE}:${DOCKER_TAG} .
                            echo $DOCKER_PASSWORD | docker login -u $DOCKER_USERNAME --password-stdin
                            docker push ${DOCKER_IMAGE}:${DOCKER_TAG}
                        '''
                    }
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
