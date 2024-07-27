pipeline {
agent { dockerfile true }

   stages {
    stage('host name') {
	    steps{
		script {
                    def osType = sh(returnStdout: true, script: 'uname -s').trim()
                    def osVersion = sh(returnStdout: true, script: 'uname -r').trim()
                    def osFlavor = sh(returnStdout: true, script: 'cat /etc/os-release | grep PRETTY_NAME').trim().split('=')[1].replaceAll('"', '')
                    echo "Running on OS: ${osType} ${osVersion} ${osFlavor}"
                }

	    }
    }
    stage('Cloning Git') {
	    steps{
	      sh 'echo checking out source code'
	    }  
     }  
 
    stage('SAST'){
      steps{
      	sh 'echo SAST stage'
	   }
    }

    
    stage('Build-and-Tag') {
    /* This builds the actual image; synonymous to
         * docker build on the command line */
      steps{	
        sh 'echo Build and Tag'
          }
    }

    stage('Post-to-dockerhub') {
     steps {
        sh 'echo post to dockerhub repo'
     }
    }

    stage('SECURITY-IMAGE-SCANNER'){
      steps {
        sh 'echo scan image for security'
     }
    }

    stage('Pull-image-server') {
      steps {
         sh 'echo pulling image ...'
       }
      }
    
    stage('DAST') {
      steps  {
         sh 'echo dast scan for security'
        }
    }
 }


}
