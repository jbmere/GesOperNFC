/*
Para lanzar la aplicación:
npx cap sync
npx cap open android
*/ 
/*
Permisos HTML CORS:
npm install cordova-plugin-advanced-http
npm install @ionic-native/http
ionic cap sync
*/
  
//Prepara los cabeceros CORS para dar permiso a la app
//if (cordova) cordova.plugin.http.setHeader('*', 'user', 'password');

const formulario = {
	nfc: [false,''],
	vehiculo: '',
	bascula: '',
	accion: '',
	peso: 0,
	photoBase64: '',
	geolocation: {
		latitude: '',
		longitude: ''
	}
};


let app = {
	// Estos parametros comprueban que se haya completado el formulario
	descarga: false,
	vehiculo: false,
	bascula: false,
	accion: false,


	initialize: () => {
		app.descargar();
		$('#pantallaInicial').fadeOut();
		app.addListeners();
	},

	addListeners: () => {
		$('label #nfc').on('change', app.nfcChange);
		$('#tipoCoche').on('change', app.carChosen);
		$('#tipoBascula').on('change', app.basculaChosen);	
		$('#tipoAccion').on('change', app.accionChosen);
		$('form #guardarPeso').on('click', app.pesoChosen);
		$('#buscarNFC').on('click', app.introducirNfc);
		$('#botonCamara').on('click', app.fotoCamara);
		$('#buttCancel').on('click', app.deletePhoto);
		$('#localizacion').on('click', app.geoLocation);
		//de prueba tambien

		$('#enviarTodo').on('click', app.formulario);
	},

  	//Descarga los vehículos que existen en la base de datos
  	//
  	descargar: () => {
		
		let options = {
	  		method: 'get',
	  		responseType: 'json'
		};
		
		if (!app.descarga) {
			app.descarga = true;
			$('#alertaDescarga').html("Se están cargando...");
			$('#alertaDescarga').css('color', 'gray');

			cordova.plugin.http.sendRequest('https://apiict00.etsii.upm.es/matriculas.php', options, 
				response => {
					const matriculas = response.data;				
					for (var key in matriculas) {
						if (matriculas.hasOwnProperty(key)) {
							$("#tipoCoche").append('<option value="' + matriculas[key]['MATRICULA'] + '"> ' + matriculas[key]['MATRICULA'] + ' </option> ').hide().show();
						};	
					};
					$('#alertaDescarga').hide();
				}, function(err) {
					$('#alertaDescarga').html("Ha ocurrido un error, reinicie la app");
					$('#alertaDescarga').css('color', 'red');
					app.descarga = false;
				}
			);
		};
  	},
  
	// Cambia el formato del texto para el NFC 
	// y manipula el botón para activarlo
	nfcChange: () => {
		var tagNfc = $('label #nfc');
		if (tagNfc.is(":checked")) {
			$('.cambio').html(" activado");
			$('.cambio').css("color", "green");
			formulario.nfc[0]=true;
			$('#buscarNFC').fadeIn();
		} else {
			$(".cambio").html(" desactivado");
			$('.cambio').css("color", "red");
			formulario.nfc[0]=false;
			$('#buscarNFC').fadeOut();
		}
	},
	
	// Guarda el coche elegido
	//
	carChosen: () => {
		var coche = $('#tipoCoche option:selected');
		formulario.vehiculo = coche.text();
		app.vehiculo = true;
	},

	// Guarda la báscula elegida
	//
	basculaChosen: () => {
		app.bascula = false;
		formulario.bascula = $('#tipoBascula option:selected').text();
		if(formulario.bascula === "Báscula Entrada Gescrap" || formulario.bascula ===  "Báscula Salida Gescrap" || formulario.bascula ===  "Báscula Entrada Cliente" || formulario.bascula ===  "Báscula Salida Cliente") {
			formulario.peso = 0;
			$('.oculta').show();
		}
		else {
			$('.oculta').hide();
			app.bascula = true;
		}
	},
	
	//Guarda el tipo de acción elegido
	//
	accionChosen: () => {
		app.accion = true;
		formulario.accion = $('#tipoAccion option:selected').text();
	},
	// Guarda el peso elegido
	//
	pesoChosen: ev => {
		ev.preventDefault();
		formulario.peso = $('form #pesoVehiculo').val();
		app.bascula = true;
	},

	// Detecta y guarda el tag NFC
	//
	introducirNfc: (ev) => {
		ev.stopPropagation();
		nfc.addNdefListener (
			function (nfcEvent) {
				var tag = nfcEvent.tag;
				var	ndefMessage = tag.ndefMessage;
				formulario.nfc[1]=ndefMessage;	
			},
			function () { // success callback
				alert("Waiting for NDEF tag");
			},
			function (error) { // error callback
				alert("Error adding NDEF listener " + JSON.stringify(error));
			}
		);
	},

	// Tomar foto con la cámara
	//
	fotoCamara: async() => {
		const { Camera } = Capacitor.Plugins;
		let options = {
			quality: 90,
			resultType: "uri",
			allowEditing: false
		};
		const image = await Camera.getPhoto(options);
		//image.path can be passed to the Filesystem API; CameraResultType.Base64 might be interesting for later
		var photoUrl = image.webPath;
		var imageCamera = document.getElementById('imgCamara');
		var buttonCamera = document.getElementById('buttCancel');
		imageCamera.style.display= 'block';				
		buttonCamera.style.display= 'block';
		$('#botonCamara').css("margin-bottom", "15px");-
		$('#buttCancel').css("margin-top", "15px");
    	imageCamera.src =  photoUrl;
    	formulario.photoUrl = photoUrl
	},
	
	// Borrar foto hecha
	//
	deletePhoto: () => {
		formulario.photoUrl = '';
		var imageCamera = document.getElementById('imgCamara');
		var buttonCamera = document.getElementById('buttCancel');
		imageCamera.style.display= 'none';
		buttonCamera.style.display= 'none';
		$('#botonCamara').css("margin-bottom", "0px");
	},
	
	//Tomar coordenadas del dispositivo
	geoLocation: async ({check=false} = {}) => {
    	const { Geolocation } = Capacitor.Plugins;
    	let options = {
		};
		var position = await Geolocation.getCurrentPosition(options);
		const coords = position.coords;
		formulario.geolocation.latitude = coords.latitude;
		formulario.geolocation.longitude = coords.longitude;
		if (!check) alert("Las coordenadas son: " + Object.values(coords));
	},

	// Comprueba si los parámetros del formulario se han introducido 
	// correctamente y permite o no enviarlo
	formulario: () => {
		if((formulario.nfc[0]===false) || ((formulario.nfc[0]===true) && (formulario.nfc[1] !== ''))){
			if(app.vehiculo) {
				if(app.bascula) {
					if(app.accion) {
						envioFormulario(formulario);	
            			alert(JSON.stringify(formulario));
					} else alert("No se ha seleccionado el tipo de acción");
				} else alert("No ha rellenado el campo de báscula");	
			} else alert("No ha seleccionado ningún vehículo");
		} else alert("No ha pasado el NFC por el tag");

		function envioFormulario(objetoEnviar) {
			let options = {
				method: 'post',
				data:  objetoEnviar
			};
			cordova.plugin.http.sendRequest('https://apiict00.etsii.upm.es/envio.php', options,
				response => {
					alert("Se ha enviado: " + response.status)
				}, function(err) {
					alert(JSON.stringify(err));
				}
			);
		};

	},

};

//Notificaciones nativas
/* document.addEventListener('deviceready', function () {
	if (navigator.notification) { // Override default HTML alert with native dialog
		window.alert = function (message) {
			navigator.notification.alert(
				message,    // message
				null,       // callback
				"Alerta", // title
				'OK'        // buttonName
			);
		};
	}
}, false); */

const ready = "deviceready";

document.addEventListener(ready, app.initialize());
