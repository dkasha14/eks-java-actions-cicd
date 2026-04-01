EKS CI/CD Pipeline with GitHub Actions, ECR, EKS, Blue-Green & Canary
📖 Overview

This project demonstrates a complete, end-to-end CI/CD pipeline that automates the building, storing, and deploying of a containerized Java application into Amazon EKS.

The setup follows modern DevOps principles:

Infrastructure as Code: Terraform (pre-provisioned platform).
Secure Authentication: OIDC integration (no static AWS credentials).
Containerization: Docker.
Orchestration: Kubernetes orchestration using Amazon EKS.
Traffic Management: Exposed externally via AWS Application Load Balancer (ALB) and Kubernetes Ingress.
Advanced Deployments: Blue-Green & Canary strategies.
Observability: Prometheus & Grafana.

The pipeline ensures that every code change is automatically built, pushed, and deployed without manual intervention while maintaining reliability and observability.

👉 The key idea is simple: Every code change automatically flows from GitHub → Build → ECR → EKS → User without manual steps.

🏗️ Architecture Flow

GitHub Repository (Application Code)
 ↓
GitHub Actions (CI/CD Pipeline Execution)
 ↓
OIDC Authentication
 ↓
AWS IAM Role Assumption
 ↓
Docker Build (Container Image Creation)
 ↓
Amazon ECR (Private Image Registry)
 ↓
Amazon EKS Cluster
 ↓
Kubernetes Pods (Running Containers)
 ↓
Kubernetes Service (NodePort / LoadBalancer)
 ↓
AWS Application Load Balancer (via Ingress)
 ↓
End User (Browser Access)

This architecture represents a secure and automated delivery pipeline. Instead of embedding AWS credentials, GitHub uses OIDC to assume an IAM role dynamically. The application is containerized and stored in ECR, while EKS handles orchestration, scaling, and self-healing. The Application Load Balancer (ALB) and Ingress expose the application externally, making the system production-ready.

Architecture Overview: This diagram visualizes the complete high-level architecture and data flow of the project. It illustrates how code pushed to GitHub triggers an OIDC-authenticated GitHub Action, which builds and pushes a Docker image to ECR. Finally, the image is deployed to an EKS cluster where traffic is managed via an Application Load Balancer, with Prometheus and Grafana handling observability.

🔁 Data Flow (End-to-End System Movement)

GitHub → CI Pipeline → Docker Image → ECR → EKS → Pods → Service → ALB (Ingress) → User

This flow represents how code transforms into a running application. Each stage passes an artifact or signal to the next stage, forming a fully automated delivery chain.

🔄 Pipeline Execution Flow
Code Trigger

Any push to the master branch triggers the GitHub Actions workflow automatically. This ensures continuous integration and continuous deployment without manual intervention.

Continuous Integration: This screenshot shows the history of GitHub Actions runs, including both failures and successful executions. It acts as an audit trail and proves that the pipeline is fully automated and continuously improving through debugging and fixes.

Authentication (OIDC-Based Access)

The pipeline uses OIDC (OpenID Connect) instead of static credentials:

GitHub generates a short-lived identity token.
AWS validates it via the IAM OIDC provider.
The IAM role github-actions-eks-role is assumed.

Benefits:

No hardcoded secrets stored in GitHub.
Temporary credentials.
Strong security posture.

Build Phase (Docker Image Creation)

The pipeline builds the Docker image:

Java application is compiled/packaged using Maven.
Dependencies are resolved.
Docker image is created.
Image is tagged using commit SHA to ensure immutability and traceability.

docker build -t dev-java-app:<commit-id> .

Automated Build Steps: This screenshot shows each stage of the GitHub Actions pipeline including AWS authentication, Docker build, and image push. Every step is automated, ensuring consistent builds across environments.

Push Phase (ECR Integration)




The Docker image is pushed to Amazon ECR:

docker push 816069164153.dkr.ecr.us-east-1.amazonaws.com/dev-java-app:<tag>

ECR acts as:

Private container registry.
Central artifact storage.
Source for Kubernetes deployments.

Private Image Registry: This screenshot shows the ECR repository storing Docker images securely.
Image Versioning: Each image is tagged with commit SHA, enabling rollback and traceability.

Cluster Access (EKS Connection)
aws eks update-kubeconfig --region us-east-1 --name EKS-DEV

This step configures kubectl so the pipeline can interact directly with the EKS cluster.

Deployment Phase (Kubernetes Apply)
kubectl apply -f k8s/

This creates:

Deployment → manages pods lifecycle
Service → exposes application
Rollout → handles canary strategy

Kubernetes ensures self-healing, scaling, and desired state.

Runtime Execution (EKS)




Initially, the deployment failed with ImagePullBackOff due to incorrect image references or IAM permission issues. After fixing, pods transitioned to Running state.

Real-World Troubleshooting: This demonstrates practical debugging using kubectl, which is critical in production systems.

External Exposure (NodePort → ALB)




The application was first exposed using NodePort as an initial validation step. In this approach, Kubernetes opens a specific port on every worker node, allowing external traffic to reach the application using the node’s public IP and port. This is extremely useful during early testing because it helps verify that pods are running correctly, services are routing traffic properly, and the application is responding as expected without introducing additional complexity.

Once the application behavior was validated, the exposure strategy was upgraded to use an AWS Application Load Balancer (ALB) through Kubernetes Ingress. This transition is critical for production systems because NodePort is not scalable or secure for public access.

With ALB:

A public DNS endpoint is automatically provisioned
Traffic is distributed across multiple pods using target groups
Health checks ensure only healthy pods receive traffic
Integration with AWS networking provides high availability across AZs
Ingress rules enable path-based and host-based routing

External Access Validation: NodePort ensured that the application and service routing were functioning correctly at a basic level. Moving to ALB introduced a production-grade traffic management layer, enabling scalability, reliability, and secure external access.

🚦 Deployment Strategies
🔵🟢 Blue-Green Deployment

Two environments are maintained simultaneously:

Blue → current stable version serving production traffic
Green → newly deployed version for validation

Instead of updating the existing deployment, a completely separate environment (Green) is created. This allows testing the new version in isolation without impacting live users. Once validation is complete, traffic is switched from Blue to Green using Kubernetes service selectors or Ingress routing.

Key advantages:

Zero downtime deployments
Instant rollback by switching traffic back to Blue
Safe validation before exposing to users

Workload Comparison: Grafana dashboards show resource usage and traffic patterns for both Blue and Green deployments. This helps confirm that the new version behaves correctly under real conditions before full cutover.

🟡 Canary Deployment

Canary deployment introduces the new version gradually instead of switching all traffic at once.

Traffic flow:

A small percentage of users is routed to the new version
System behavior is continuously monitored
If stable, traffic is increased step-by-step
If issues occur, rollout is stopped or rolled back immediately

This strategy is particularly useful in production systems where even minor bugs can impact users.

Benefits:

Reduced blast radius of failures
Real-time validation with actual user traffic
Data-driven decision making using metrics

This approach ensures that new releases are introduced safely without risking the entire system.

🚀 Argo Rollouts (Progressive Delivery)

Argo Rollouts extends Kubernetes deployment capabilities by enabling progressive delivery with fine-grained traffic control.

Instead of a single deployment update, traffic is shifted in controlled stages:

20% → initial exposure
50% → partial rollout
100% → full production

Between each stage, the rollout can pause automatically, allowing time to monitor system metrics and validate stability.

Key capabilities:

Automated canary and blue-green strategies
Traffic shaping without redeploying services
Integration with monitoring tools for automated decisions

This makes deployments predictable, observable, and reversible, which is critical for production-grade systems.

📊 Monitoring (Prometheus + Grafana)

Monitoring ensures that the system is not only running but also performing optimally under load.

Prometheus collects time-series metrics from Kubernetes components, nodes, and applications
Grafana visualizes these metrics in dashboards for easy analysis

Key metrics observed:

CPU utilization (node + pod level)
Memory consumption
Pod health and restart counts
Namespace-level resource usage

Monitoring plays a crucial role during deployments:

Detect performance degradation during Canary rollout
Compare Blue vs Green workload behavior
Identify bottlenecks or resource exhaustion

Cluster Observability: Grafana dashboards provide real-time visibility into the cluster’s health and performance. This allows proactive detection of issues, better capacity planning, and confident deployment decisions.

External Exposure (NodePort → ALB)




The application was first exposed using NodePort as an initial validation step. In this approach, Kubernetes opens a specific port on every worker node, allowing external traffic to reach the application using the node’s public IP and port. This is extremely useful during early testing because it helps verify that pods are running correctly, services are routing traffic properly, and the application is responding as expected without introducing additional complexity.

Once the application behavior was validated, the exposure strategy was upgraded to use an AWS Application Load Balancer (ALB) through Kubernetes Ingress. This transition is critical for production systems because NodePort is not scalable or secure for public access.

With ALB:

A public DNS endpoint is automatically provisioned
Traffic is distributed across multiple pods using target groups
Health checks ensure only healthy pods receive traffic
Integration with AWS networking provides high availability across AZs
Ingress rules enable path-based and host-based routing

External Access Validation: NodePort ensured that the application and service routing were functioning correctly at a basic level. Moving to ALB introduced a production-grade traffic management layer, enabling scalability, reliability, and secure external access.

🚦 Deployment Strategies
🔵🟢 Blue-Green Deployment

Two environments are maintained simultaneously:

Blue → current stable version serving production traffic
Green → newly deployed version for validation

Instead of updating the existing deployment, a completely separate environment (Green) is created. This allows testing the new version in isolation without impacting live users. Once validation is complete, traffic is switched from Blue to Green using Kubernetes service selectors or Ingress routing.

Key advantages:

Zero downtime deployments
Instant rollback by switching traffic back to Blue
Safe validation before exposing to users

Workload Comparison: Grafana dashboards show resource usage and traffic patterns for both Blue and Green deployments. This helps confirm that the new version behaves correctly under real conditions before full cutover.

🟡 Canary Deployment

Canary deployment introduces the new version gradually instead of switching all traffic at once.

Traffic flow:

A small percentage of users is routed to the new version
System behavior is continuously monitored
If stable, traffic is increased step-by-step
If issues occur, rollout is stopped or rolled back immediately

This strategy is particularly useful in production systems where even minor bugs can impact users.

Benefits:

Reduced blast radius of failures
Real-time validation with actual user traffic
Data-driven decision making using metrics

This approach ensures that new releases are introduced safely without risking the entire system.

🚀 Argo Rollouts (Progressive Delivery)

Argo Rollouts extends Kubernetes deployment capabilities by enabling progressive delivery with fine-grained traffic control.

Instead of a single deployment update, traffic is shifted in controlled stages:

20% → initial exposure
50% → partial rollout
100% → full production

Between each stage, the rollout can pause automatically, allowing time to monitor system metrics and validate stability.

Key capabilities:

Automated canary and blue-green strategies
Traffic shaping without redeploying services
Integration with monitoring tools for automated decisions

This makes deployments predictable, observable, and reversible, which is critical for production-grade systems.

📊 Monitoring (Prometheus + Grafana)

Monitoring ensures that the system is not only running but also performing optimally under load.

Prometheus collects time-series metrics from Kubernetes components, nodes, and applications
Grafana visualizes these metrics in dashboards for easy analysis

Key metrics observed:

CPU utilization (node + pod level)
Memory consumption
Pod health and restart counts
Namespace-level resource usage

Monitoring plays a crucial role during deployments:

Detect performance degradation during Canary rollout
Compare Blue vs Green workload behavior
Identify bottlenecks or resource exhaustion

Cluster Observability: Grafana dashboards provide real-time visibility into the cluster’s health and performance. This allows proactive detection of issues, better capacity planning, and confident deployment decisions.

🧠 Concepts Demonstrated

CI/CD automation using GitHub Actions
Secure AWS authentication using OIDC
Docker image lifecycle management
Kubernetes deployments, services, ingress
Blue-Green and Canary strategies
Argo Rollouts
Monitoring with Prometheus & Grafana
Real-world debugging

⭐ Summary

This project showcases a real-world, production-grade DevOps pipeline where CI/CD, security, containerization, Kubernetes, deployment strategies, and monitoring work together to deliver a fully automated and reliable application deployment system.
