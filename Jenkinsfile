pipeline {
    agent {
        docker {
            image 'rimsdk/maven-docker:latest'
            args '''
                --privileged
                -v /var/run/docker.sock:/var/run/docker.sock
                -v $HOME/.docker:/root/.docker
            '''
        }
    }

    environment {
        DOCKER_IMAGE = "rimsdk/banking-app"
        DOCKER_TAG = "latest"
        // Définir le répertoire de configuration Docker
        DOCKER_CONFIG = "/root/.docker"
    }

    stages {
        stage('Build') {
            steps {
                echo "Construction de l'application avec Maven..."
                sh 'mvn clean package'
            }
        }

        stage('Test') {
            steps {
                echo "Exécution des tests unitaires..."
                sh 'mvn test'
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Docker Build & Push') {
            steps {
                echo "Construction et publication de l'image Docker..."
                withCredentials([usernamePassword(
                    credentialsId: 'dockerhub-credentials',
                    usernameVariable: 'DOCKER_USERNAME',
                    passwordVariable: 'DOCKER_PASSWORD'
                )]) {
                    sh '''
                        docker build -t ${DOCKER_IMAGE}:${DOCKER_TAG} .
                        mkdir -p ${DOCKER_CONFIG}
                        echo $DOCKER_PASSWORD | docker login -u $DOCKER_USERNAME --password-stdin
                        docker push ${DOCKER_IMAGE}:${DOCKER_TAG}
                    '''
                }
            }
        }
    }

    post {
        always {
            echo "Nettoyage de l'environnement de travail..."
            cleanWs()
        }
    }
}