{{/*
Expand the name of the chart.
*/}}
{{- define "matete-ping-pong.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Create a default fully qualified app name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
If release name contains chart name it will be used as a full name.
*/}}
{{- define "matete-ping-pong.fullname" -}}
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
{{- define "matete-ping-pong.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Common labels
*/}}
{{- define "matete-ping-pong.labels" -}}
helm.sh/chart: {{ include "matete-ping-pong.chart" . }}
{{ include "matete-ping-pong.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}


{{/*
Common labels ping
*/}}
{{- define "matete-ping-pong-ping.labels" -}}
helm.sh/chart: {{ include "matete-ping-pong.chart" . }}
{{ include "matete-ping-pong-ping.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}


{{/*
Common labels pong
*/}}
{{- define "matete-ping-pong-pong.labels" -}}
helm.sh/chart: {{ include "matete-ping-pong.chart" . }}
{{ include "matete-ping-pong-pong.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}



{{/*
Selector labels
*/}}
{{- define "matete-ping-pong.selectorLabels" -}}
app.kubernetes.io/name: {{ include "matete-ping-pong.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}




{{/*
Selector labels ping
*/}}
{{- define "matete-ping-pong-ping.selectorLabels" -}}
app.kubernetes.io/name: {{ include "matete-ping-pong-ping.container" . }}
app.kubernetes.io/instance: {{ .Release.Name }}-ping
{{- end }}



{{/*
Selector labels pong
*/}}
{{- define "matete-ping-pong-pong.selectorLabels" -}}
app.kubernetes.io/name: {{ include "matete-ping-pong-pong.container" . }}
app.kubernetes.io/instance: {{ .Release.Name }}-pong
{{- end }}

{{/*
 Broker name
*/}}
{{- define "matete-ping-pong.brokerName" -}}
{{ .Release.Name}}-kafka:{{ .Values.kafka.service.ports.client }}
{{- end }}


{{/*
container ping
*/}}
{{- define "matete-ping-pong-ping.container" -}}
{{ include "matete-ping-pong.fullname" . }}-ping
{{- end }}

{{/*
container pong
*/}}
{{- define "matete-ping-pong-pong.container" -}}
{{ include "matete-ping-pong.fullname" . }}-pong
{{- end }}


{{/*
container port
*/}}
{{- define "matete-ping-pong.container.port" -}}
{{ .Values.mateteClientContainerPort }}
{{- end }}

{{/*
Create the name of the service account to use
*/}}
{{- define "matete-ping-pong.serviceAccountName" -}}
{{- if .Values.serviceAccount.create }}
{{- default (include "matete-ping-pong.fullname" .) .Values.serviceAccount.name }}
{{- else }}
{{- default "default" .Values.serviceAccount.name }}
{{- end }}
{{- end }}
