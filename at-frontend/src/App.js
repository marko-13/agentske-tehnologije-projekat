import React from 'react';
import './App.css';

import Particles from 'react-particles-js';
import Axios from 'axios';

const particleOptions = {
  particles: {
    number: {
      value: 90,
      density: {
        enable: true,
        value_area: 700
      }
    }
  },
  interactivity: {
    events: {
        onhover: {
            enable: true,
            mode: "repulse"
        }
    }
  }
}



class App extends React.Component {

  state = {
    agentsTypes: [],
    agentsRunning: [],

  }

  socket = null;
  host = "ws://" + window.location.host + "/2020WAR/ws/";
  sessionId = '';

  //FUNKCIJA ZA PRETVARANJE TIMESTAMP U DATUM
  //*******************************
  timeConverter = (timestamp) => {
  const a = new Date(timestamp * 1000);
  const months = ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'];
  const year = a.getFullYear();
  const month = months[a.getMonth()];
  const date = a.getDate();
  const hour = a.getHours();
  const min = a.getMinutes();
  //const sec = a.getSeconds();
  const time = date + ' ' + month + ' ' + year + ' ' + hour + ':' + min;
  return time;
  }
  //*******************************

  componentDidMount() {

    Axios.get('rest/agents/running')
    .then( res => {
      console.log('pogodio get agents running')
    })
    .catch(err => alert('Error occured while getting running agents'));

    try{
      this.socket = new WebSocket(this.host + window.location.host);
      console.log('connect: Socket Status: ' + this.socket.readyState);
      console.log('Session opened on location: ' + window.location.host);

      this.socket.onopen = (evt) => {
        console.log('onopen: Socket Status: ' + this.socket.readyState + ' (open)');
      }
      // ISPRAVLJENO teoretski korisnici mogu medjusobno da se brisu
      // ako posalju poruku deleteUser:neko_korisnicko_ime
      // ali da se ne pravi poseban websocket za sad ostaje ova implementacija
      this.socket.onmessage = function(msg) {
        // const msgObject = JSON.parse(msg.data);
        // alert(msgObject);
        // // received public message
        // if(msgObject.category === 0) {
        //   $('#divMsg').append('<div class="container"><h4>From: '+msgObject.sender+'</h4><h5>Subject: '+msgObject.subject+'</h5><p>'+msgObject.content+'</p><hr></div>');
        // }
        // // received private message
        // else if(msgObject.category === 1){
        //   $('#divMsg').append('<div class="container"><h4>From: '+msgObject.sender+'</h4><h5>Subject: '+msgObject.subject+'</h5><p>'+msgObject.content+'</p><hr></div>');
        // }
        // // add user to active users list
        // else if(msgObject.category === 2){
        //   console.log('add user: ' + msgObject.content + ' to active users list');
        //   $('#selectUser').append(new Option(msgObject.content, msgObject.content));
        // }
        // // remove user from active users list
        // else if(msgObject.category === 3){
        //   console.log('delete user: ' + msgObject.content + ' from active users list');
        //   $('#selectUser option[value=' + msgObject.content + ']').remove();
        // }
      }
      
      this.socket.onclose = function() {
        console.log("WS closed")
        sessionStorage.setItem('username', '');
        this.socket = null;
      }

    } catch(e) {
      console.log('Socket exception: ' + e);
    }

  }
  
    render() {
      return(
      <div>
      <Particles className='particles' params={particleOptions}/>

      <h1>TEST</h1>
      </div>)
    }

}

export default App;
