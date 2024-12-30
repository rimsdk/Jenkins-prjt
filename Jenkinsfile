pipeline {
    agent {
        docker {
            image 'rimsdk/maven-docker:latest'
            args '''
                --privileged
                -v /var/run/docker.sock:/var/run/docker.sock
            '''
        }
    }

    environment {
        DOCKER_IMAGE = "rimsdk/banking-app"
        DOCKER_TAG = "latest"
        DOCKER_CONFIG = "/tmp/.docker"
    }

    stages {
        stage('Build') {
            steps {
                echo "Construction de l'application avec Maven..."
                sh 'mvn clean package -DskipTests'
            }
        }

        stage('Test') {
            steps {
                echo "Exécution des tests unitaires..."
                sh 'mvn test'
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml' // Publication des résultats des tests
                }
                success {
                    echo 'Tests réussis !'
                }
                failure {
                    echo 'Tests échoués !'
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
                        mkdir -p ${DOCKER_CONFIG}
                        docker build -t ${DOCKER_IMAGE}:${DOCKER_TAG} .
                        DOCKER_CONFIG=${DOCKER_CONFIG} echo $DOCKER_PASSWORD | docker login -u $DOCKER_USERNAME --password-stdin
                        docker push ${DOCKER_IMAGE}:${DOCKER_TAG}
                    '''
                }
            }
        }
    }

    post {
        always {
            echo "Nettoyage de l'environnement de travail..."
            node { // Contexte pour cleanWs
                cleanWs()
            }
            sh 'rm -rf /tmp/.docker'
        }
    }
}
