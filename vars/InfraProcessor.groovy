#!/usr/bin/env groovy

def call(body) {
  	// evaluate the body block, and collect configuration into the object
  	def config = [:]
  	body.resolveStrategy = Closure.DELEGATE_FIRST
  	body.delegate = config
  	body()
  
  	def branch = env.BRANCH_NAME
  	def mergebranch = "main"
  	if(body!=null && body['mergebranch']	!=null){
		mergebranch = body['mergebranch']
  	}
  	config['mergebranch'] = mergebranch
  	
 	pipeline {
		options {
        	buildDiscarder(logRotator(numToKeepStr: '10'))
    	} 
        agent any
        stages { 
			stage('verify-account'){
				steps {
      					script { 
   		    				if(body==null || body['account']==null || body['account']== ""){
							println "*****************************************"
							println " Jenkinsfile does not contain 'account' number, Add it to the Jenkinsfile"
							println "****************************************"
							throw new Exception("Jenkinsfile does not contain 'account' number, Add it to the Jenkinsfile")
						}
					}
				}	
			}
   			stage('checkout') {
     			steps {
      				checkout scm
				script { 
   		    			env.TARGET_AWS_ACCOUNT = body["account"]
      					env.mode = "prd"
      					// env.BUILD_AWS_ACCOUNT = sh ( script: "curl -s -S 'http://169.254.169.254/latest/dynamic/instance-identity/document/' | jq -r '.accountId'", returnStdout: true)
      					env.JENKINS_FQDN= sh (script: 'echo ${BUILD_URL/https:\\/\\/} | cut -d "/" -f1', returnStdout: true).trim()
      					env.CODE_AUTHOR = sh (script: "git log -1 --no-merges --format='%ae' ${GIT_COMMIT}", returnStdout: true).trim()	
						env.CODE_MERGED = sh (script: "git log -1 --format='%ae' ${GIT_COMMIT}", returnStdout: true).trim()
						println "===================================================================="
			        	println "CHANGE_ID             : " + env.CHANGE_ID
			        	println "BRANCH_NAME           : " + env.BRANCH_NAME
			        	println "GIT_URL               : " + env.GIT_URL
			        	println "GIT_COMMIT            : " + env.GIT_COMMIT
			        	// println "BUILD_AWS_ACCOUNT_ID  : " + env.BUILD_AWS_ACCOUNT.trim()
						println "TARGET_AWS_ACCOUNT_ID : " + env.TARGET_AWS_ACCOUNT
			        	println "JENKINS_FQDN          : " + env.JENKINS_FQDN
			        	println "CHANGE_TARGET         : " + env.CHANGE_TARGET
			        	println "CHANGE_URL            : " + env.CHANGE_URL
						println "CODE_AUTHOR           : " + env.CODE_AUTHOR
						println "CODE_MERGED           : " + env.CODE_MERGED
			        	println "===================================================================="
					}	
			        
				}	
    		}
			stage('verify'){
				when{ 
					expression { return env.GIT_COMMIT } 
				} 
				steps{
					sh '''
						git diff-tree --no-commit-id --name-only -r $GIT_COMMIT
					'''
				}
			}
			stage('validate'){
				steps {
					script {
						if (!env.BRANCH_NAME.equals(mergebranch)) {
							println "*****************************************"
							println " Running Template Validation "
							println "****************************************"
							validateCf(config)
						}
					}
				}	
			}
			stage('process'){	
				steps {
					script {
						if (env.BRANCH_NAME.equals(mergebranch)) {
							println "*****************************************"
							println " Calling mergeRequest"
							println "****************************************"
							mergeRequest(config)
						} else {
							println "*****************************************"
							println " Calling pullRequest"
							println "****************************************"
							pullRequestProcess(config)
						}
					}	
				}	
			}
			stage('end') {
				steps {
			 		echo "*****************************************"
 					echo " Return from job - Complete!"
  					echo "*****************************************"				
				}
			}	
		}
		post {
			always {
				script {
					if (env.BRANCH_NAME.equals(mergebranch)) {

						env.GIT_COMMITTER_EMAIL = sh(
						script: "git --no-pager show -s --format='%ae'",
							returnStdout: true
						).trim()	

						emailext mimeType: 'text/html', 
							body: "${currentBuild.currentResult}: Job ${env.JOB_NAME} build ${env.BUILD_NUMBER} <br/> More info at: ${env.BUILD_URL}",
							recipientProviders: [
								[$class: 'CulpritsRecipientProvider'],
								[$class: 'DevelopersRecipientProvider'],
								[$class: 'RequesterRecipientProvider']
							], 
							subject: "Jenkins Build ${currentBuild.currentResult}: Job ${env.JOB_NAME}",
							to: "${env.CODE_AUTHOR},${env.CODE_MERGED}"

						build wait: false, job: 'build-log-uploader', 
                                parameters: [string(name: 'INVOKER_JOB_URL', value: env.JOB_URL)
                                , string(name: 'INVOKER_JOB_NAME', value: env.JOB_NAME)
                                , string(name: 'INVOKER_BUILD_NUMBER', value: env.BUILD_NUMBER)]	
					}
				}
				cleanWs()
			}
	 	} 
    }
}
