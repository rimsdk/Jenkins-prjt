pipeline {
    agent any

    tools {
        maven 'Maven_3.9.9'  // Spécifier la version de Maven à utiliser
    }

    environment {
        DOCKER_IMAGE = "mehdi/banking-app"  // Nom de l'image Docker
        DOCKER_TAG = "${BUILD_NUMBER}"  // Tag basé sur le numéro de build
        DOCKER_INSTALL_DIR = "/var/jenkins_home/docker"  // Répertoire d'installation de Docker
    }

    stages {
        stage('Setup Docker') {
            steps {
                script {
                    // Vérification et installation de Docker si nécessaire
                    sh '''
                        if ! command -v docker &> /dev/null; then
                            mkdir -p ${DOCKER_INSTALL_DIR}
                            curl -fsSL https://get.docker.com -o ${DOCKER_INSTALL_DIR}/get-docker.sh
                            chmod +x ${DOCKER_INSTALL_DIR}/get-docker.sh
                            sh ${DOCKER_INSTALL_DIR}/get-docker.sh --dry-run
                            # Installation de Docker via une méthode alternative
                            curl -fsSL https://download.docker.com/linux/static/stable/x86_64/docker-20.10.9.tgz -o ${DOCKER_INSTALL_DIR}/docker.tgz
                            tar xzvf ${DOCKER_INSTALL_DIR}/docker.tgz -C ${DOCKER_INSTALL_DIR}
                            export PATH=${DOCKER_INSTALL_DIR}/docker:$PATH
                            # Lancement du démon Docker si non en cours
                            if ! pgrep dockerd > /dev/null; then
                                ${DOCKER_INSTALL_DIR}/docker/dockerd &
                                sleep 10  # Attendre que le démon Docker démarre
                            fi
                        fi
                        # Vérification de l'installation de Docker
                        docker --version || true
                    '''
                }
            }
        }

        stage('Checkout') {
            steps {
                // Clonage du dépôt Git
                git branch: 'main', url: 'https://github.com/Mehdi-ben17/Jenkins-Project.git'
            }
        }

        stage('Build') {
            steps {
                // Exécution de la commande Maven pour construire le projet
                sh 'mvn clean package -DskipTests'
            }
        }

        stage('Test') {
            steps {
                // Exécution des tests unitaires avec Maven
                sh 'mvn test'
            }
            post {
                always {
                    // Collecte des résultats des tests
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Docker Build & Push') {
            steps {
                script {
                    // Construction et push de l'image Docker vers Docker Hub
                    withCredentials([usernamePassword(
                        credentialsId: 'dockerhub-credentials',
                        usernameVariable: 'DOCKER_USERNAME',
                        passwordVariable: 'DOCKER_PASSWORD'
                    )]) {
                        sh """
                            export PATH=${DOCKER_INSTALL_DIR}/docker:\$PATH
                            echo \$DOCKER_PASSWORD | docker login -u \$DOCKER_USERNAME --password-stdin
                            docker build -t \$DOCKER_USERNAME/${DOCKER_IMAGE}:${DOCKER_TAG} .
                            docker push \$DOCKER_USERNAME/${DOCKER_IMAGE}:${DOCKER_TAG}
                        """
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
                    // Déploiement de l'application sur le serveur distant
                    sh """
                        export PATH=${DOCKER_INSTALL_DIR}/docker:\$PATH
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
            // Nettoyage des fichiers temporaires après le pipeline
            cleanWs()
        }
    }
}
