new Vue({
  el: '#app',
  data() {
    return {
      systemHealth: {
        cpuTemperature: 0.0
      }
    }
  },
  mounted() {
    let socket = new SockJS('/ws')
    let stompClient = Stomp.over(socket)
    stompClient.connect({}, (frame) => {
      console.log('Connected: ' + frame)
      stompClient.subscribe('/telemetry/health', (data) => {
        console.info(data.body)
        this.systemHealth = JSON.parse(data.body)
      })
    })
  }
})
