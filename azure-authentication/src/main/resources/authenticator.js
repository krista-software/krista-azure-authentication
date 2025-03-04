let Auth = (() => {

  let baseUrl = "";
  let context = null;

  const constructUrl = (path) => {
    let url = baseUrl;
    if (url.lastIndexOf("/") === url.length) {
      url = url + path;
    } else {
      url = url + "./" + path;
    }
    return url;
  };

  const setContext = ({ data, url }) => {
    context = data;
    baseUrl = url;
  };

    const doLogin = (payload) => {
        return new Promise((resolve, reject) => {
            fetch(constructUrl("../authn/authorize"), {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(payload),
            })
                .then(response => response.json())
                .then(data => {
                    let isAuthenticatedSuccessfully = false;
                    const authWindow = window.open(data.authUrl, '_blank', 'width=600,height=400');

                    const messageHandler = (event) => {
                        if (event.origin !== window.location.origin) return; // Validate origin
                        if (event.data && event.data.clientSessionId) {
                            const clientSessionId = event.data.clientSessionId;

                            fetch(constructUrl("../authn/login"), {
                                method: "POST",
                                headers: {
                                    "Content-Type": "application/json",
                                    "clientSessionId": clientSessionId,
                                },
                            })
                                .then(response => {
                                    if (!response.ok) {
                                        throw new Error(`HTTP error! status: ${response.status}`);
                                    }
                                    return response.json();
                                })
                                .then(loginData => {
                                    isAuthenticatedSuccessfully = true;
                                    window.removeEventListener("message", messageHandler); // Cleanup
                                    resolve(loginData);
                                })
                                .catch(error => {
                                    window.removeEventListener("message", messageHandler); // Cleanup
                                    reject(`LOGIN API ERROR: ${error.message}`);
                                });
                        }
                    };

                    window.addEventListener("message", messageHandler);

                    const checkWindowClosed = setInterval(() => {
                        if (!isAuthenticatedSuccessfully && authWindow && authWindow.closed) {
                            clearInterval(checkWindowClosed);
                            window.removeEventListener("message", messageHandler);
                            reject("AZURE AUTH WINDOW CLOSED...");
                        }
                    }, 500);
                })
                .catch(error => reject(`AZURE LOGIN FAILED: ${error.message}`));
        });
    };


  const doDecorateRequest = () => {
    let headers = {};
    if (context && context.clientSessionId) {
      headers = {
        "X-Krista-Context": encodeURIComponent(
          JSON.stringify({
            clientSessionId: context.clientSessionId,
          })
        ),
      };
    }
    return headers;
  };

  const doLogout = () => {
    return new Promise((resolve, reject) => {
      fetch(constructUrl("../authn/logout"), {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          ...doDecorateRequest(),
        },
        body: JSON.stringify({
          clientSessionId: context.clientSessionId,
        }),
      })
        .then((response) => response.json())
        .then((data) => {
          resolve(data);
        })
        .catch((error) => {
          reject(`Error in loading chatbot.`);
        });
    });
  };
  const getForm = () => {
    return "";
  };

  const formValues = (values) => {
    const payload = {};
    values.forEach((item) => {
      payload[item.name] = item.value;
    });
    return payload;
  };

  const validatePayload = (payload) => {
    let error = "";
    if (payload.email === "") {
      error = "Enter email";
    }
    return error;
  };

  return {
    init: setContext,
    login: doLogin,
    logout: doLogout,
    decorateRequest: doDecorateRequest,
    getFromFields: getForm,
    getPayload: formValues,
    validatePayload: validatePayload,
  };

})();

window.Authenticator = Auth;
