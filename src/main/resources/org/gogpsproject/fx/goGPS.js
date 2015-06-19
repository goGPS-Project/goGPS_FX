console.log("goGPS.js");
if (!document.getElementById('FirebugLite')){
	E = document['createElement' + 'NS'] && document.documentElement.namespaceURI;
	E = E ? document['createElement' + 'NS'](E, 'script') : document['createElement']('script');
	E['setAttribute']('id', 'FirebugLite');
//	E['setAttribute']('src', 'http://getfirebug.com/releases/lite/latest/firebug-lite-debug.js');
	E['setAttribute']('src', 'js/firebug-lite/firebug-lite-debug.js');
	E['setAttribute']('FirebugLite', '4');
	E.onload = function() {
		  console.log(Firebug);
		  console.log(Firebug.chrome);
		  console.log(Firebug.chrome.toggle);
		  Firebug.chrome.toggle();
     };
	(document['getElementsByTagName']('head')[0] || document['getElementsByTagName']('body')[0]).appendChild(E);
//	E = new Image;
//	E['setAttribute']('src', 'https://getfirebug.com/#startOpened=false');
//  }  
};


