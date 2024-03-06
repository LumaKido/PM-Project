const Sequelize = require("sequelize")
const sequelize = new Sequelize("test", "root", "", { 
    host: "localhost", 
    dialect: "mysql" 
})

sequelize.authenticate().then(function () { 
    console.log("Conectado com sucesso!") 
}).catch(function (erro) { 
    console.log("Erro ao conectar: " + erro) 
})

const Agendamentos = sequelize.define("agendamentos", { 
    nome: { 
        type: Sequelize.STRING 
    }, endereco: { 
        type: Sequelize.STRING 
    }, bairro: { 
        type: Sequelize.STRING 
    }, cep: { 
        type: Sequelize.STRING 
    }, cidade: { 
        type: Sequelize.STRING 
    }, estado: { 
        type: Sequelize.STRING 
    } 
})
//Agendamentos.sync({force: true})

/*Agendamentos.create({    
    nome: "Luma Mari Pereira Kido",    
    endereco: "Av Aguia de Haia, 2500",    
    bairro: "Jardim Cotinha",    
    cep: "08543-436",    
    cidade: "São Paulo",    
    estado: "SP"
})
*/

module.exports = Agendamentos