document.body.addEventListener('htmx:configRequest', e => {
    console.log(e.detail)
    let num = e.detail.triggeringEvent.srcElement.getAttribute('value')
    e.detail.parameters['num'] = num
})
