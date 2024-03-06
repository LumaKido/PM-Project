const express = require("express")
const app = express()
const agendamentos = require("./banco")

app.listen(8081, function(){    
    console.log("Servidor Ativo!")
})

app.get("/", function(req,res){
    res.send("Página Principal")
})
   
app.get("/cadastrar/:nome/:endereco/:bairro/:cep/:cidade/:estado", function(req,res){
      agendamentos.create({
      nome: req.params.nome,
      endereco: req.params.endereco,
      bairro: req.params.bairro,
      cep: req.params.cep,
      cidade: req.params.cidade,
      estado: req.params.estado
    })
      res.send("Dados cadastrados com sucesso!")
})