pipeline {
    agent any

    tools {
        maven 'Maven_3.8.5'
    }

    environment {
        DOCKER_IMAGE = "mehdi/banking-app"
        DOCKER_TAG = "${BUILD_NUMBER}"
        DOCKER_INSTALL_DIR = "/var/jenkins_home/docker"
    }

    stages {
        stage('Setup Docker') {
            steps {
                script {
                    // Ajout des permissions Docker pour Jenkins
                    sh '''
                        # Création du groupe docker si non existant
                        sudo groupadd -f docker

                        # Ajout de l'utilisateur jenkins au groupe docker
                        sudo usermod -aG docker jenkins

                        # Modification des permissions du socket Docker
                        sudo chmod 666 /var/run/docker.sock

                        # Vérification de l'installation de Docker
                        docker --version || true

                        # Redémarrage du service Docker si nécessaire
                        sudo service docker restart || true

                        # Attente que le socket soit disponible
                        sleep 5
                    '''
                }
            }
        }

        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/rimsdk/Jenkins-prjt.git'
            }
        }

        stage('Build') {
            steps {
                sh 'mvn clean package -DskipTests'
            }
        }

        stage('Test') {
            steps {
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
                script {
                    withCredentials([usernamePassword(
                        credentialsId: 'dockerhub-credentials',
                        usernameVariable: 'DOCKER_USERNAME',
                        passwordVariable: 'DOCKER_PASSWORD'
                    )]) {
                        sh '''
                            # Vérification des permissions
                            ls -l /var/run/docker.sock
                            groups jenkins

                            # Construction et push de l'image
                            docker build -t ${DOCKER_USERNAME}/${DOCKER_IMAGE}:${DOCKER_TAG} .
                            echo $DOCKER_PASSWORD | docker login -u $DOCKER_USERNAME --password-stdin
                            docker push ${DOCKER_USERNAME}/${DOCKER_IMAGE}:${DOCKER_TAG}
                        '''
                    }
                }
            }
        }

        stage('Deploy') {
            steps {
                withCredentials([sshUserPrivateKey(
                    credentialsId: 'ssh-key',
                    keyFileVariable: 'SSH_KEY'
                )]) {
                    sh """
                        ssh -i \$SSH_KEY -o StrictHostKeyChecking=no user@remote-server '
                            docker pull \$DOCKER_USERNAME/${DOCKER_IMAGE}:${DOCKER_TAG} &&
                            docker stop banking-app || true &&
                            docker rm banking-app || true &&
                            docker run -d --name banking-app \$DOCKER_USERNAME/${DOCKER_IMAGE}:${DOCKER_TAG}
                        '
                    """
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