{{/*
Expand the name of the chart.
*/}}
{{- define "covid.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Create a default fully qualified app name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
If release name contains chart name it will be used as a full name.
*/}}
{{- define "covid.fullname" -}}
{{- if .Values.fullnameOverride }}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- $name := default .Chart.Name .Values.nameOverride }}
{{- if contains $name .Release.Name }}
{{- .Release.Name | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}
{{- end }}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "covid.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Common labels
*/}}
{{- define "covid.labels" -}}
helm.sh/chart: {{ include "covid.chart" . }}
{{ include "covid.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}




{{/*
service covid
*/}}
{{- define "covid.service.name" -}}
{{ include "covid.fullname" . }}-svc
{{- end }}


{{/*
container covid
*/}}
{{- define "covid.container.name" -}}
{{ include "covid.fullname" . }}
{{- end }}

{{/*
container port
*/}}
{{- define "covid.container.port" -}}
{{ .Values.service.port }}
{{- end }}


{{/*
Selector labels
*/}}
{{- define "covid.selectorLabels" -}}
app.kubernetes.io/name: {{ include "covid.container.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{/*
Create the name of the service account to use
*/}}
{{- define "covid.serviceAccountName" -}}
{{- if .Values.serviceAccount.create }}
{{- default (include "covid.fullname" .) .Values.serviceAccount.name }}
{{- else }}
{{- default "default" .Values.serviceAccount.name }}
{{- end }}
{{- end }}



{{/*
 Kafka broker name
*/}}
{{- define "covid.kafka.brokerName" -}}
{{ .Release.Name}}-kafka:{{ .Values.kafka.service.ports.client }}
{{- end }}




{{/*
 postgres port
*/}}
{{- define "covid.postgresql.port" -}}
{{ .Values.postgresql.primary.service.ports.postgresql }}
{{- end }}



{{/*
 postgres host
*/}}
{{- define "covid.postgresql.host" -}}
{{ .Release.Name}}-postgresql
{{- end }}


{{/*
 postgres container name
*/}}
{{- define "covid.postgres.container.name" -}}
{{ include "covid.fullname" . }}-postgres
{{- end }}





