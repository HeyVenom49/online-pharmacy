window.onload = function() {
  window.ui = SwaggerUIBundle({
    url: "/v3/api-docs",
    dom_id: '#swagger-ui',
    deepLinking: true,
    presets: [
      SwaggerUIBundle.presets.apis,
      SwaggerUIStandalonePreset
    ],
    plugins: [
      SwaggerUIBundle.plugins.DownloadUrl
    ],
    layout: "StandaloneLayout",
    configUrl: "/v3/api-docs/swagger-config",
    displayRequestDuration: true,
    operationsSorter: "method",
    persistAuthorization: true,
    supportedSubmitMethods: ["get", "post", "put", "patch", "delete"],
    tagsSorter: "alpha",
    tryItOutEnabled: true,
    validatorUrl: "",
    withCredentials: true
  });
  ui.initOAuth({
    useBasicAuthenticationWithAccessCodeGrant: false
  });
};
